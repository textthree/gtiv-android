package com.dqd2022.page.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.databinding.MineFragmentBinding;
import com.dqd2022.helpers.AlertUtils;
import com.dqd2022.helpers.App;

import kit.LogKit;
import kit.NumberKit;

public class MineFragment extends Fragment {
    MainActivity activity;
    MineFragmentBinding binding;

    public MineFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MineFragmentBinding.inflate(inflater, container, false);
        initData();
        return binding.getRoot();
    }

    void initData() {
        binding.avatar.setImageURI(App.myAvatar);
        binding.nickname.setText(App.myNickname);
        binding.userAccount.setText(getString(R.string.userid) + " " + App.myUsername);
        binding.fansNum.setText(NumberKit.formatWithUnit(App.language, App.fansNum));
        binding.followNum.setText(NumberKit.formatWithUnit(App.language, App.followNum));
        binding.supportNum.setText(NumberKit.formatWithUnit(App.language, App.supportNum));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 申请主播
        binding.anchor.setOnClickListener(l -> {
            AlertUtils.toast(getString(R.string.apply_anchor_tips));
        });
        // 账号管理
        binding.manager.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineManager);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
        // 我的发布
        binding.myRelease.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineMyRelease);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
        // 我的收藏
        binding.myCollect.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineMyCollect);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
        // 点赞列表
        binding.support.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineSupportMeList);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
        // 关注列表
        binding.follow.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineFollowList);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
        // 粉丝列表
        binding.fans.setOnClickListener(l -> {
            Intent i = new Intent(activity, MineManagerActivity.class);
            i.putExtra("fragmentKey", FragmentKey.mineFansList);
            activity.startActivity(i);
            activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        });
    }

    @Override
    public void onResume() {
        LogKit.p("mineFragment onResume");
        super.onResume();
        initData();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}