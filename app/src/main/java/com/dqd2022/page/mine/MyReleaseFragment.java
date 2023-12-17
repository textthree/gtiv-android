package com.dqd2022.page.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.dqd2022.api.VideosApi;
import com.dqd2022.databinding.MineMyVideosBinding;
import com.dqd2022.databinding.VideoGridListItemBinding;
import com.dqd2022.dto.VideoApiDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.video.SinglePlayActivity;

import java.util.ArrayList;

import kit.LogKit;
import kit.NumberKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReleaseFragment extends Fragment {
    MineMyVideosBinding binding;
    Activity activity;
    ListAdapter adapter;
    int page = 1;
    private ArrayList<VideoApiDto.UserVideoItem> videoList;
    private String endpoint;
    boolean hasMore = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MineMyVideosBinding.inflate(inflater, container, false);
        activity = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 返回
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            activity.finish();
        });
        initData();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void initData() {
        // 列表
        VideosApi.getInstance().userCrationVideos(App.myUserId, page).enqueue(new Callback<VideoApiDto.UserVideosResponse>() {
            @Override
            public void onResponse(Call<VideoApiDto.UserVideosResponse> call, Response<VideoApiDto.UserVideosResponse> response) {
                VideoApiDto.UserVideosResponse res = response.body();
                videoList = res.List;
                if (res.List.size() == 0) {
                    binding.noData.container.setVisibility(View.VISIBLE);
                    binding.noData.text.setText(getString(R.string.noCreationVideo));
                    return;
                }
                endpoint = res.Endpoint;
                initVideoList();
                if (App.myCreatVideoNum > 12) {
                    loadMore();
                }
            }

            @Override
            public void onFailure(Call<VideoApiDto.UserVideosResponse> call, Throwable t) {
            }
        });
        // 数量
        String count = getString(R.string.video_count).toString().replace("?", String.valueOf(App.myCreatVideoNum));
        binding.titleCount.setText(count);
    }

    void initVideoList() {
        binding.list.setLayoutManager(new GridLayoutManager(activity, 3));
        adapter = new ListAdapter();
        binding.list.setAdapter(adapter);
        // 监听滑动事件
        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        VideosApi.getInstance().userCrationVideos(App.myUserId, ++page).enqueue(new Callback<VideoApiDto.UserVideosResponse>() {
            @Override
            public void onResponse(Call<VideoApiDto.UserVideosResponse> call, Response<VideoApiDto.UserVideosResponse> response) {
                VideoApiDto.UserVideosResponse res = response.body();
                int count = res.List.size();
                if (count > 0) {
                    int itemPosition = videoList.size();
                    for (int i = 0; i < count; i++) {
                        videoList.add(res.List.get(i));
                        adapter.notifyItemInserted(itemPosition);
                        itemPosition++;
                    }
                } else {
                    hasMore = false;
                }
            }

            @Override
            public void onFailure(Call<VideoApiDto.UserVideosResponse> call, Throwable t) {
            }
        });
    }

    private class ListAdapter extends RecyclerView.Adapter<holder> {

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            VideoGridListItemBinding view = VideoGridListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull holder holder, int position) {
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
                    intent.putExtra("title", item.Title);
                    intent.putExtra("uri", url);
                    intent.putExtra("masterUid", App.myUserId);
                    intent.putExtra("masterAvatar", App.myAvatar);
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

    private class holder extends RecyclerView.ViewHolder {
        VideoGridListItemBinding view;

        public holder(@NonNull VideoGridListItemBinding itemView) {
            super(itemView.getRoot());
            view = itemView;
        }
    }


}