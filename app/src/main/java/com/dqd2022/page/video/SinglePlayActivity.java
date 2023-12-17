package com.dqd2022.page.video;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.transition.Fade;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dqd2022.R;
import com.dqd2022.api.UsersApi;
import com.dqd2022.api.VideosApi;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.databinding.SinglePlayActivityBinding;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.dto.VideoApiDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.userpage.UserPageActivity;

import kit.ImageKit;
import kit.LogKit;
import kit.MathKit;
import kit.MmkvKit;
import kit.NumberKit;
import kit.StatusBar.StatusBarKit;
import kit.glide.GlideCircleBorderTransform;
import kit.video.PlayerHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class SinglePlayActivity extends AppCompatActivity {
    private SinglePlayActivityBinding binding;
    private boolean isShowSeekBar = false;
    ImageView playIcon, pauseIcon;
    SurfaceView sfv;
    PlayerHolder playerHolder;
    String playingUri;
    Handler mHandler;
    int initProgress, videoWidth, videoHeight;
    String title, videoUrl, videoCover;
    boolean landscape, releaseVideo; // releaseVideo 是否是在 app 上发布的视频，播放聊天时从相册选择的视频为非 releaseVideo
    VideoModel videoModel;
    String masterAvatar = "";
    int supportNum, collectNum, masterUid, videoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        binding = SinglePlayActivityBinding.inflate(getLayoutInflater());
        playIcon = binding.fullscreenPlayIcon;
        pauseIcon = binding.fullscreenPauseIcon;
        getWindow().setEnterTransition(new Fade());
        initData();
        if (landscape) {
            setLandscapeScreen();
        } else {
            StatusBarKit.setBgBlackAndFontWhite(this);
        }
        setContentView(binding.getRoot());
        playVideo();
        initView();
        if (releaseVideo) getMasterInfo();
    }

    @Override
    protected void onPause() {
        if (landscape) cancelLandscapeScreen();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        close();
    }

    @Override
    public void onStop() {
        playerHolder.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        if (playerHolder != null) playerHolder.destroy();
    }

    void close() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putLong("progress", playerHolder.getPlayer().getCurrentPosition());
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    void initData() {
        Intent i = getIntent();
        title = i.getStringExtra("title");
        binding.title.setText(title);
        videoUrl = i.getStringExtra("uri");
        videoModel = new VideoModel();
        playingUri = videoModel.getVideoUri(this, videoUrl);
        long l = i.getLongExtra("progress", 0);
        initProgress = (int) l;
        landscape = i.getBooleanExtra("landscape", false);
        releaseVideo = i.getBooleanExtra("releaseVideo", false);
        masterAvatar = i.getStringExtra("masterAvatar");
        masterUid = i.getIntExtra("masterUid", 0);
        videoId = i.getIntExtra("videoId", 0);

    }

    void initView() {
        if (landscape) {
            initLandscapeScreen();
        } else {
            initVerticalScreen();
        }
        // 返回
        binding.back.setOnClickListener(v -> {
            close();
        });
        getWindow().setNavigationBarColor(App.bottomNaviBgBlack);
        // 点击头像
        binding.avatar.setOnClickListener(l -> {
            Intent intent = new Intent(this, UserPageActivity.class);
            intent.putExtra("avatar", masterAvatar);
            intent.putExtra("userId", masterUid);
            startActivity(intent);
        });
    }

    // 初始化竖屏视图
    void initVerticalScreen() {
        if (releaseVideo) binding.rightBar.setVisibility(View.VISIBLE);
        binding.seekBar.setVisibility(View.VISIBLE);
        binding.titleBar.setVisibility(View.VISIBLE);
        binding.fullScreenSurfaceview.setOnClickListener(l -> {
            if (playerHolder.getPlayer().isPlaying()) {
                playerHolder.pause();
            } else {
                playerHolder.start();
            }
        });
        // intent 带过来的头像
        if (masterAvatar != null && !masterAvatar.equals("")) {
            setAvatar(masterAvatar);
        }
        // 关注
        binding.follow.setOnClickListener(l -> {
            binding.follow.setImageURI(ImageKit.drawable2uri(this, R.drawable.home_video_follow_success));
            UsersApi.getInstance().follow(masterUid).enqueue(CommonResDto.commonCallback);
            new android.os.Handler().postDelayed(() -> {
                binding.follow.setVisibility(View.INVISIBLE);
            }, 2000);

        });
        if (!releaseVideo) return;
        // 点赞
        binding.support.setOnClickListener(l -> {
            String key = MMKVkey.supportVideos.name();
            if (!MmkvKit.HashSetContains(key, videoId)) {
                // 点赞
                binding.support.setImageURI(ImageKit.drawable2uri(getApplication(), R.drawable.home_video_supported));
                MmkvKit.HashSetAdd(key, videoId);
                supportNum++;
                VideosApi.getInstance().support(videoId, 1).enqueue(CommonResDto.commonCallback);
            } else {
                // 取消
                binding.support.setImageURI(ImageKit.drawable2uri(getApplicationContext(), R.drawable.home_video_support));
                MmkvKit.HashSetRemoveItem(key, videoId);
                supportNum--;
                VideosApi.getInstance().support(videoId, 0).enqueue(CommonResDto.commonCallback);
            }
            binding.supportCount.setText(NumberKit.formatWithUnit(App.language, supportNum));
        });
        // 收藏
        binding.collect.setOnClickListener(l -> {
            String key = MMKVkey.collectVideos.name();
            if (!MmkvKit.HashSetContains(key, videoId)) {
                // 收藏
                binding.collect.setImageURI(ImageKit.drawable2uri(getApplicationContext(), R.drawable.home_video_collected));
                MmkvKit.HashSetAdd(key, videoId);
                collectNum++;
                VideosApi.getInstance().collect(videoId, 1).enqueue(CommonResDto.commonCallback);
            } else {
                // 取消
                binding.collect.setImageURI(ImageKit.drawable2uri(getBaseContext(), R.drawable.home_video_collect));
                MmkvKit.HashSetRemoveItem(key, videoId);
                collectNum--;
                VideosApi.getInstance().collect(videoId, 0).enqueue(CommonResDto.commonCallback);
            }
            binding.collectNum.setText(NumberKit.formatWithUnit(App.language, collectNum));
        });
        // 分享
        binding.share.setOnClickListener(l -> {
            Intent i = new Intent(this, ShareToContactsActivity.class);
            i.putExtra("videoId", videoId);
            i.putExtra("cover", videoCover);
            i.putExtra("width", videoWidth);
            i.putExtra("height", videoHeight);
            i.putExtra("uri", videoUrl);
            LogKit.p("videoUrl", videoUrl);
            startActivity(i);
        });

    }

    void setAvatar(String uri) {
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_avatar).circleCropTransform();
        Glide.with(this).asBitmap().load(uri).apply(options).
                transform(new GlideCircleBorderTransform(6, Color.parseColor("#ffffff"))).
                into(binding.avatar);
    }

    // 初始化横屏视图
    void initLandscapeScreen() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.titleBar.getLayoutParams();
        //params.topMargin = MathKit.dp2px(this, 50);
        binding.titleBar.setLayoutParams(params);
        // 点击背景
        binding.videoFullscreen.setOnClickListener(v -> {
            if (!isShowSeekBar) {
                isShowSeekBar = true;
                binding.seekBar.setVisibility(View.VISIBLE);
                binding.titleBar.setVisibility(View.VISIBLE);
                if (playerHolder.getPlayer().isPlaying()) {
                    pauseIcon.setVisibility(View.VISIBLE);
                    pauseIcon.setImageAlpha(100);
                } else {
                    playIcon.setVisibility(View.VISIBLE);
                    playIcon.setImageAlpha(100);
                }
            } else {
                isShowSeekBar = false;
                binding.seekBar.setVisibility(View.GONE);
                binding.titleBar.setVisibility(View.GONE);
                playIcon.setVisibility(View.GONE);
                pauseIcon.setVisibility(View.GONE);
            }
        });
        // 点击暂停按钮
        pauseIcon.setOnClickListener(v -> {
            pauseIcon.setVisibility(View.GONE);
            playIcon.setVisibility(View.VISIBLE);
            playIcon.setImageAlpha(100);
            playerHolder.pause();
        });
        // 点击播放按钮
        playIcon.setOnClickListener(v -> {
            playIcon.setVisibility(View.GONE);
            binding.seekBar.setVisibility(View.GONE);
            binding.titleBar.setVisibility(View.GONE);
            playerHolder.start();
        });
    }


    // 去除标题栏、状态栏、虚拟导航按键栏
    void setLandscapeScreen() {
        // 设置屏幕方向为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // 取消横屏、还原样式
    void cancelLandscapeScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    // 播放
    void playVideo() {
        if (binding.seekBar != null) binding.seekBar.setProgress(0);
        if (playerHolder != null) playerHolder.destroy();
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        sfv = binding.fullScreenSurfaceview;
        if (landscape) {
            playerHolder = new PlayerHolder(this, sfv, PlayerHolder.Scale.scaleY);
        } else {
            playerHolder = new PlayerHolder(this, sfv, PlayerHolder.Scale.scaleX);
        }
        playerHolder.playAsync(playingUri, initProgress);
        // 走带
        playerHolder.setOnPlayerListener(new PlayerHolder.OnPlayerListener() {
            int delay = 100; // 200 毫秒走一次

            @Override
            public void onStart(IjkMediaPlayer ijk) {
                final long duration = ijk.getDuration();
                binding.seekBar.setMax((int) duration);
                if (initProgress > 0) {
                    binding.seekBar.setProgress(initProgress);
                }
                mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                if (playerHolder != null && playerHolder.getPlayer().isPlaying()) {
                                    binding.seekBar.setProgress((int) ijk.getCurrentPosition());
                                }
                                sendEmptyMessageDelayed(0, delay);
                                break;
                        }
                    }
                };
                mHandler.sendMessageDelayed(mHandler.obtainMessage(0), delay);
            }

            @Override
            public void onPlayComplete(IjkMediaPlayer ijk) {
                playVideo();
            }
        });
        initProgress = 0;
        // 进度条监听器
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int target;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 进度发生改变时会触发
                if (fromUser) {
                    target = progress;

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
    }


    void getMasterInfo() {
        VideosApi.getInstance().getVideoInfo(videoId).enqueue(new Callback<VideoApiDto.VideoInfoResDto>() {
            @Override
            public void onResponse(Call<VideoApiDto.VideoInfoResDto> call, Response<VideoApiDto.VideoInfoResDto> response) {
                VideoApiDto.VideoInfoResDto res = response.body();
                if (masterAvatar.equals("")) {
                    setAvatar(res.MasterAvatar);
                }
                supportNum = res.SupportNum;
                collectNum = res.CollectNum;
                masterUid = res.MasterUid;
                videoCover = res.VideoCover;
                videoWidth = res.VideoWidth;
                videoHeight = res.VideoHeight;
                binding.supportCount.setText(NumberKit.formatWithUnit(App.language, supportNum));
                binding.collectNum.setText(NumberKit.formatWithUnit(App.language, collectNum));
                binding.shareNum.setText(NumberKit.formatWithUnit(App.language, res.ShareNum));
                // 是否已关注
                if (!res.IsFollow) {
                    binding.follow.setVisibility(View.VISIBLE);
                }
                // 是否已点赞
                if (MmkvKit.HashSetContains(MMKVkey.supportVideos.name(), videoId)) {
                    binding.support.setImageURI(ImageKit.drawable2uri(getBaseContext(), R.drawable.home_video_supported));
                }
                // 是否已收藏
                if (MmkvKit.HashSetContains(MMKVkey.collectVideos.name(), videoId)) {
                    binding.collect.setImageURI(ImageKit.drawable2uri(getApplication(), R.drawable.home_video_collected));
                }
            }

            @Override
            public void onFailure(Call<VideoApiDto.VideoInfoResDto> call, Throwable t) {
            }
        });

    }


}