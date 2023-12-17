package com.dqd2022.page.userpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dqd2022.R;
import com.dqd2022.databinding.UserHomePageviewFragmentBinding;
import com.dqd2022.databinding.VideoGridListItemBinding;
import com.dqd2022.dto.VideoApiDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.video.SinglePlayActivity;

import java.util.ArrayList;

import kit.LogKit;
import kit.NumberKit;


public class PageViewFragment extends Fragment {
    private UserPageModle model;
    private UserHomePageviewFragmentBinding binding;
    private int position;
    private ArrayList<VideoApiDto.UserVideoItem> videoList;
    private String endpoint;
    private int userId;
    private String masterAvatar;
    private volatile int creationListPage = 1;
    private volatile int suppportListPage = 1;
    VideoAdapter adapter;
    boolean hasMore = true;
    Activity activity;


    public PageViewFragment(int position) {
        this.position = position;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = new UserPageModle();
        binding = UserHomePageviewFragmentBinding.inflate(inflater, container, false);
        Bundle b = getArguments();
        userId = b.getInt("userId");
        masterAvatar = b.getString("avatar", "");
        initData();
        return binding.getRoot();
    }

    void initVideoList() {
        RecyclerView list = binding.userHomeRecyclerview;
        GridLayoutManager layout = new GridLayoutManager(getActivity(), 3);
        list.setLayoutManager(layout);
        adapter = new VideoAdapter();
        list.setAdapter(adapter);
        // 监听滑动事件
        //  binding.userHomeRecyclerview.requestDisallowInterceptTouchEvent(true);
        binding.userHomeRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    LogKit.p("到底部了");
                    loadMore();
                }
            }
        });
    }

    void loadMore() {
        if (!hasMore) {
            return;
        }
        if (position == 0) {
            model.getUserCreationVideos((resp -> {
                int count = resp.List.size();
                if (count > 0) {
                    int itemPosition = videoList.size();
                    for (int i = 0; i < count; i++) {
                        videoList.add(resp.List.get(i));
                        adapter.notifyItemInserted(itemPosition);
                        itemPosition++;
                    }
                } else {
                    hasMore = false;
                }
            }), userId, ++creationListPage);
        } else if (position == 1) {
            model.getUserCreationVideos((resp -> {
                int count = resp.List.size();
                if (count > 0) {
                    int itemPosition = videoList.size();
                    for (int i = 0; i < count; i++) {
                        videoList.add(resp.List.get(i));
                        adapter.notifyItemInserted(itemPosition);
                        itemPosition++;
                    }
                } else {
                    hasMore = false;
                }
            }), userId, ++suppportListPage);
        }
    }

    void initData() {
        if (position == 0) {
            model.getUserCreationVideos(resp -> {
                videoList = resp.List;
                endpoint = resp.Endpoint;
                initVideoList();
                loadMore();
            }, userId, creationListPage);
        } else if (position == 1) {
            model.getUserSupportVideos(resp -> {
                videoList = resp.List;
                endpoint = resp.Endpoint;
                initVideoList();
                loadMore();
            }, userId, suppportListPage);
        }
    }


    private class videoHolder extends RecyclerView.ViewHolder {
        VideoGridListItemBinding view;

        public videoHolder(VideoGridListItemBinding itemView) {
            super(itemView.getRoot());
            view = itemView;
        }
    }

    private class VideoAdapter extends RecyclerView.Adapter<videoHolder> {
        @NonNull
        @Override
        public videoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            VideoGridListItemBinding view = VideoGridListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new videoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull videoHolder holder, int position) {
            if (videoList.size() > 0) {
                VideoApiDto.UserVideoItem item = videoList.get(position);
                if (!item.Cover.equals("")) {
                    String cover = endpoint + item.Cover;
                    Glide.with(activity.getBaseContext())
                            .load(cover)
                            .placeholder(R.drawable.video_cover_loading)
                            .centerCrop()
                            .into(holder.view.cover);
                }
                holder.view.supportNum.setText(NumberKit.formatWithUnit(App.language, item.SupportNum));
                holder.view.cover.setOnClickListener(l -> {
                    Intent intent = new Intent(getActivity(), SinglePlayActivity.class);
                    String url = endpoint + item.Cover.replace("cover.jpg", "index.m3u8");
                    LogKit.p("endpoint", url);
                    intent.putExtra("title", item.Title);
                    intent.putExtra("uri", url);
                    intent.putExtra("masterUid", userId);
                    intent.putExtra("masterAvatar", masterAvatar);
                    intent.putExtra("videoId", item.VideoId);
                    intent.putExtra("releaseVideo", true);
                    activity.startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return videoList.size();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}