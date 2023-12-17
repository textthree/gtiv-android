package com.dqd2022.page.im.chatting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dqd2022.databinding.ChattingAssetsViewFragmentBinding;
import com.dqd2022.helpers.App;

import kit.LogKit;
import kit.StatusBar.StatusBarKit;

public class AssetsViewFragment extends Fragment {
    private View mContentView;
    private ChattingAssetsViewFragmentBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        StatusBarKit.translucentStatus(getActivity());
        binding = ChattingAssetsViewFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContentView = binding.container;
        mContentView.setOnClickListener(v -> {
            colse();
        });
        String uri = getArguments().getString("uri");
        Glide.with(App.context).load(uri).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                binding.loading.container.setVisibility(View.GONE);
                return false;
            }
        }).into(binding.photo);
        binding.photo.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentView = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    // 关闭当前 fragment
    private void colse() {
        Activity activity = getActivity();
        StatusBarKit.setBgWhiteAndFontBlack(activity);
        ChattingActivity chattingActivity = (ChattingActivity) activity;
        FragmentManager fm = chattingActivity.getSupportFragmentManager();
        FragmentTransaction beginTransaction = fm.beginTransaction();
        beginTransaction.hide(this);
        beginTransaction.commit();
    }


}

