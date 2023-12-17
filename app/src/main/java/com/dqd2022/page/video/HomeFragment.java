package com.dqd2022.page.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.databinding.VideoHomeFragmentBinding;
import com.dqd2022.dto.VideoPlaylistItemDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.page.userpage.UserPageActivity;

import java.util.ArrayList;

import kit.FileKit;
import kit.ImageKit;
import kit.LogKit;
import kit.MmkvKit;
import kit.NumberKit;
import kit.glide.GlideCircleBorderTransform;
import kit.video.PlayerHolder;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class HomeFragment extends Fragment {
    private VideoHomeFragmentBinding binding;
    VideoViewModel viewModel;
    VideoModel model;
    private ArrayList<VideoPlaylistItemDto> playlist = new ArrayList();
    PlayerHolder playerHolder;
    SurfaceView surfaceView;
    int playingPosition = 0;
    Handler mHandler;
    SeekBar seekbar;
    ActivityResultLauncher<Intent> resultLauncher;
    Activity activity;
    VideoAction action;
    View pageView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
        // 监听全屏播放/分享 Activity 返回数据
        resultLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result != null) {
                        Intent intent = result.getData();
                        if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                            String msg = intent.getStringExtra("message");
                            if (msg != null && msg.equals("switchToMessageList")) {
                                MainActivity main = (MainActivity) activity;
                                main.clickNav("message");
                                return;
                            }
                            int progress = (int) intent.getLongExtra("progress", 0);
                            play(binding.homeVideoList, playingPosition, progress);
                        }
                    }
                }
        );
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = VideoHomeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        viewModel.context = activity;
        model = new VideoModel();
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        binding.networkError.setOnClickListener(l -> {
            initView();
        });
    }


    void initView() {
        viewModel.getVideoList((count) -> {
            if (count == 0) {
                binding.networkError.setVisibility(View.VISIBLE);
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.closeSplashPage();
                return;
            }
            LogKit.p("onCreateView 初始化视频列表成功");
            binding.networkError.setVisibility(View.GONE);
            VideoListLayoutManager layoutManager = new VideoListLayoutManager(activity, OrientationHelper.VERTICAL, false);
            slideListener(layoutManager); // 添加上下滑动监听器
            RecyclerView list = binding.homeVideoList;
            list.setLayoutManager(layoutManager);
            list.setAdapter(new VideoAdapter());
        }, playlist);
    }

    private void slideListener(VideoListLayoutManager layoutManager) {
        layoutManager.setViewPagerListener(new VideoListLayoutManager.OnViewPagerListener() {
            @Override
            public void onShowPage(View view, int position, int surplus) {
                LogKit.p("切换播放：" + position + " 剩余：" + surplus);
                if (position != playingPosition) {
                    play(view, position);
                }
                if (surplus < 5) {
                    viewModel.getVideoListAsync(playlist);
                }
            }

            @Override
            public void onRemovePage(int position) {
                viewModel.stopCacheTask(position);
            }

            @Override
            public void onBeforShowPage(View view) {
                view.findViewById(R.id.video_home_cover).setVisibility(View.VISIBLE);
            }
        });
    }

    private class videoHolder extends RecyclerView.ViewHolder {
        public videoHolder(View itemView) {
            super(itemView);
        }

        public void initItemView(View view, VideoPlaylistItemDto item) {
            action = new VideoAction(activity, view, model);
            // 头像
            Uri uri = Uri.parse(item.getAvatar());
            ImageView avatar = view.findViewById(R.id.video_home_avatar);
            RequestOptions options = new RequestOptions().placeholder(R.drawable.default_avatar).circleCropTransform();
            Glide.with(App.context).asBitmap().load(uri).apply(options).
                    transform(new GlideCircleBorderTransform(6, Color.parseColor("#ffffff"))).
                    into(avatar);
            view.findViewById(R.id.video_home_avatar).setOnClickListener(v -> {
                Intent intent = new Intent(activity, UserPageActivity.class);
                intent.putExtra("progress", playerHolder.getPlayer().getCurrentPosition());
                intent.putExtra("nick", item.getNickname());
                intent.putExtra("avatar", item.getAvatar());
                intent.putExtra("userId", item.getUserId());
                resultLauncher.launch(intent);
            });
            // 关注
            ImageView img = view.findViewById(R.id.video_home_follow);
            if (item.isFollow()) {
                img.setVisibility(View.INVISIBLE);
            }
            action.follow(img, item.getUserId());

            // 点赞（是否点赞，本地保存就行了，点赞记录存后端）
            TextView support = view.findViewById(R.id.video_home_support_count);
            support.setText(NumberKit.formatWithUnit(App.language, item.getSupportNum()));
            img = view.findViewById(R.id.video_home_support);
            if (MmkvKit.HashSetContains(MMKVkey.supportVideos.name(), item.getVideoId())) {
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_supported));
            }
            action.support(img, item.getVideoId(), item.getSupportNum());

            // 收藏
            TextView collect = view.findViewById(R.id.video_home_collect_count);
            collect.setText(NumberKit.formatWithUnit(App.language, item.getCollectNum()));
            img = view.findViewById(R.id.video_home_collect);
            if (MmkvKit.HashSetContains(MMKVkey.collectVideos.name(), item.getVideoId())) {
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_collected));
            }
            action.collect(img, item.getVideoId(), item.getCollectNum());

            // 分享
            TextView share = view.findViewById(R.id.video_home_share_count);
            share.setText(NumberKit.formatWithUnit(App.language, item.getShareNum()));
            Long progresss = 0l;
            if (playerHolder != null) progresss = playerHolder.getPlayer().getCurrentPosition();
            action.share(resultLauncher, item, progresss);

            // 昵称
            TextView nick = view.findViewById(R.id.video_home_nickname);
            nick.setText(item.getNickname());
            // 标题
            TextView title = view.findViewById(R.id.video_home_title);
            title.setText(item.getTitle());
        }

    }

    private class VideoAdapter extends RecyclerView.Adapter<videoHolder> {

        @NonNull
        @Override
        public videoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.from(parent.getContext()).inflate(R.layout.video_home_item, parent, false);
            return new videoHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull videoHolder holder, int position) {
            View pageRootView = holder.itemView;
            VideoPlaylistItemDto item = playlist.get(position);
            holder.initItemView(pageRootView, item);
            ImageView img = pageRootView.findViewById(R.id.video_home_cover);
            String coverUri = item.getCover();
            if (FileKit.exists(coverUri)) {
                // 等比缩放图片 - 图片宽高从后端获得
                int[] coverWH = viewModel.coverFullWidth(item.getWidth(), item.getHeight());
                ViewGroup.LayoutParams params = img.getLayoutParams();
                params.width = coverWH[0];
                params.height = coverWH[1];
                img.setLayoutParams(params);
                img.setImageURI(Uri.parse(item.getCover()));
                img.setVisibility(View.VISIBLE);
            } else {
                // 来一张正在加载的动图
                //img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.default_avatar));
            }
            // 自动播放
            if (playingPosition == 0) {
                play(pageRootView, 0);
                MainActivity mainActivity = (MainActivity) activity;
                new Handler().postAtTime(() -> mainActivity.closeSplashPage(), 1000);
            }
        }

        @Override
        public int getItemCount() {
            return playlist.size();
        }
    }


    // 播放
    public void play(View pageView, int position, int... initProgress) {
        this.pageView = pageView;
        pageView.findViewById(R.id.home_video_play_icon).setVisibility(View.GONE);
        VideoPlaylistItemDto item = playlist.get(position);
        ImageView icon = pageView.findViewById(R.id.home_video_play_icon);
        if (icon != null) icon.setVisibility(View.GONE);
        if (seekbar != null) seekbar.setProgress(0);
        if (playerHolder != null) playerHolder.destroy();
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        surfaceView = pageView.findViewById(R.id.video_home_surface);
        playerHolder = new PlayerHolder(activity, surfaceView, PlayerHolder.Scale.scaleX);
        String url = model.getVideoUri(activity, item.getVideoUri());
        playerHolder.playAsync(url, initProgress);
        onClickSurfaceView();
        playingPosition = position;
        seekbar = pageView.findViewById(R.id.home_video_seekBar);
        ImageView cover = pageView.findViewById(R.id.video_home_cover);
        // 走带
        playerHolder.setOnPlayerListener(new PlayerHolder.OnPlayerListener() {
            int delay = 100; // 100 毫秒走一次

            @Override
            public void onStart(IjkMediaPlayer ijk) {
                final long duration = ijk.getDuration();
                int width = ijk.getVideoWidth();
                int height = ijk.getVideoHeight();
                //LogKit.p("开始播放，视频长度:", duration);
                if (width > height) {
                    pageView.findViewById(R.id.full_screen_play).setVisibility(View.VISIBLE);
                }
                seekbar.setMax((int) duration);
                // 从全屏返回时
                if (initProgress.length > 0) {
                    seekbar.setProgress(initProgress[0]);
                }
                mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                if (playerHolder.getPlayer().isPlaying()) {
                                    cover.setVisibility(View.INVISIBLE);
                                    seekbar.setProgress((int) ijk.getCurrentPosition());
                                }
                                sendEmptyMessageDelayed(0, delay);
                                break;
                        }
                    }
                };
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0), delay);
            }

            // 重播网络 m3u8 需要重置播放器，本地已缓存的 mp4 可以不用重新创建播放器
            @Override
            public void onPlayComplete(IjkMediaPlayer ijk) {
                // 重播
                play(pageView, playingPosition);
            }
        });
        // 进度条监听
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int target;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 进度发生改变时会触发
                if (fromUser) {
                    target = progress;
                    long duration = playerHolder.getPlayer().getDuration();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 按下 SeekBar 时会触发
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 松开 SeekBar 时触发
                playerHolder.getPlayer().seekTo(target);
            }
        });
        // 全屏观看
        pageView.findViewById(R.id.full_screen_play).setOnClickListener((v) -> {
            playerHolder.getPlayer().stop();
            Intent intent = new Intent(activity, SinglePlayActivity.class);
            intent.putExtra("uri", playerHolder.getPlayingUri());
            intent.putExtra("title", playlist.get(position).getTitle());
            intent.putExtra("progress", playerHolder.getPlayer().getCurrentPosition());
            intent.putExtra("landscape", true);
            resultLauncher.launch(intent);
            pageView.findViewById(R.id.home_video_play_icon).setVisibility(View.GONE);
        });
        // 缓存视频
        viewModel.cacheVideo(position, url);
    }

    private void onClickSurfaceView() {
        surfaceView.setOnClickListener(v -> {
            ImageView icon = binding.homeVideoList.findViewById(R.id.home_video_play_icon);
            if (playerHolder != null && playerHolder.getPlayer().isPlaying()) {
                // 暂停
                playerHolder.pause();
                icon.setVisibility(View.VISIBLE);
                icon.setImageAlpha(100);
            } else {
                // 从暂停中恢复播放
                playerHolder.start();
                icon.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LogKit.p("onResume voidFragment");
        //ImHelpers.totalBadgeReduce();
    }

    @Override
    public void onStop() {
        if (playerHolder != null) {
            playerHolder.pause();
            // TODO 暂时用第一帧的封面盖住，尝试使用 TextureView 代替 SurfaceView 解决黑屏问题
            // TextureView 不行的话切换后台时对当前 progress 进行截图盖住
            pageView.findViewById(R.id.video_home_cover).setVisibility(View.VISIBLE);
        }
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (playerHolder != null) playerHolder.pause();
        } else {
            if (playerHolder != null) playerHolder.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
    }


}