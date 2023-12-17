package com.dqd2022.page.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqd2022.databinding.LiveListFragmentBinding;
import com.dqd2022.databinding.LiveListItemBinding;
import com.dqd2022.dto.LiveListItemDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.login.LoginActivity;

import java.util.List;

public class LiveFragment extends Fragment {
    LiveModel model;
    LiveViewModel viewModel;
    private LiveListFragmentBinding binding;
    List<LiveListItemDto> liveListData;
    String hlsEndpoint;
    String rtmpEndpoint;
    RecyclerView list;
    Activity activity;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(LiveViewModel.class);
        model = new LiveModel();
        binding = LiveListFragmentBinding.inflate(inflater, container, false);
        list = binding.liveList;
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        model.getList((liveListData, hls, rtmp) -> {
            this.liveListData = liveListData;
            this.hlsEndpoint = hls;
            this.rtmpEndpoint = rtmp;
            list.setAdapter(new LiveListAdapter());
        });
        activity = getActivity();
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class LiveListAdapter extends RecyclerView.Adapter<videoHolder> {

        @NonNull
        @Override
        public videoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LiveListItemBinding view = LiveListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new videoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull videoHolder holder, int position) {
            LiveListItemDto item = liveListData.get(position);
            // title
            holder.view.liveRoomTitle.setText(item.Title);
            // state
            if (item.State == 1) {
                holder.view.state1.setVisibility(View.VISIBLE);
                holder.view.state0.setVisibility(View.GONE);
                holder.view.cover.setImageAlpha(230);
                holder.view.playIcon.setVisibility(View.VISIBLE);
            } else {
                holder.view.state1.setVisibility(View.GONE);
                holder.view.state0.setVisibility(View.VISIBLE);
                holder.view.cover.setImageAlpha(200);
                holder.view.playIcon.setVisibility(View.GONE);
            }
            // cover
            holder.view.cover.setImageURI(item.Cover);
            // avatar、nick
            holder.view.avatar.setImageURI(item.getAvatar());
            holder.view.nickname.setText(item.Nickname);
            // follow
            if (item.IsFollow) {
                holder.view.follow.setVisibility(View.GONE);
                holder.view.unfollow.setVisibility(View.VISIBLE);
                holder.view.unfollow.setOnClickListener(l -> {
                    if (!App.isLogin()) {
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        return;
                    }
                    holder.view.follow.setVisibility(View.VISIBLE);
                    holder.view.unfollow.setVisibility(View.GONE);
                    model.followOrUnfollw(item.UserId);
                    item.IsFollow = false;
                    initItemView(holder.view, item);
                });
            } else {
                holder.view.follow.setVisibility(View.VISIBLE);
                holder.view.unfollow.setVisibility(View.GONE);
                holder.view.follow.setOnClickListener(l -> {
                    if (!App.isLogin()) {
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        return;
                    }
                    holder.view.follow.setVisibility(View.GONE);
                    holder.view.unfollow.setVisibility(View.VISIBLE);
                    model.followOrUnfollw(item.UserId);
                    item.IsFollow = true;
                    initItemView(holder.view, item);
                });
            }
            initItemView(holder.view, item);
        }

        @Override
        public int getItemCount() {
            return liveListData.size();
        }
    }

    private class videoHolder extends RecyclerView.ViewHolder {
        LiveListItemBinding view;

        public videoHolder(LiveListItemBinding item) {
            super(item.getRoot());
            view = item;
        }
    }


    void initItemView(LiveListItemBinding view, LiveListItemDto item) {
        view.cover.setOnClickListener((listen) -> {
            if (item.State == 1) {
                Intent i = new Intent(getActivity(), LiveDetailActivity.class);
                i.putExtra("title", item.Title);
                i.putExtra("masterId", item.UserId);
                i.putExtra("roomId", item.RoomId);
                i.putExtra("hls", hlsEndpoint);
                i.putExtra("rtmp", rtmpEndpoint);
                i.putExtra("isFollow", item.IsFollow);
                i.putExtra("nick", item.Nickname);
                i.putExtra("avatar", item.getAvatar());
                getActivity().startActivity(i);
            }
        });
    }


}