package com.dqd2022.page.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dqd2022.databinding.LiveDetailActivityBinding;
import com.dqd2022.helpers.App;
import com.dqd2022.page.login.LoginActivity;

import kit.LogKit;
import kit.StatusBar.StatusBarKit;
import kit.video.PlayerHolder;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

// 刷新页面数据时不能通过创建新的 Fragment 方式，会导致 MainActivity 失去引用
public class LiveDetailActivity extends AppCompatActivity {
    private LiveDetailActivityBinding binding;
    PlayerHolder player;
    String roomId;
    int masterId;
    String hlsEndopint;
    String rtmpEndopint;
    LiveModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        binding = LiveDetailActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        StatusBarKit.setBgBlackAndFontWhite(this);
        model = new LiveModel();
        initView();
        play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) player.destroy();
    }

    void initView() {
        Intent intent = getIntent();
        masterId = intent.getIntExtra("masterId", 0);
        roomId = intent.getStringExtra("roomId");
        rtmpEndopint = intent.getStringExtra("rtmp");
        hlsEndopint = intent.getStringExtra("hls");
        String title = intent.getStringExtra("title");
        String avatar = intent.getStringExtra("avatar");
        String nick = intent.getStringExtra("nick");
        boolean isFollow = intent.getBooleanExtra("isFollow", false);
        binding.liveDetailTitle.setText(title);
        binding.avatar.setImageURI(avatar);
        binding.nickname.setText(nick);
        if (isFollow) {
            binding.follow.setVisibility(View.GONE);
            binding.unfollow.setVisibility(View.VISIBLE);
            binding.unfollow.setOnClickListener(l -> {
                if (!App.isLogin()) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return;
                }
                binding.follow.setVisibility(View.VISIBLE);
                binding.unfollow.setVisibility(View.GONE);
                model.followOrUnfollw(masterId);
            });
        } else {
            binding.follow.setVisibility(View.VISIBLE);
            binding.unfollow.setVisibility(View.GONE);
            binding.follow.setOnClickListener(l -> {
                if (!App.isLogin()) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return;
                }
                binding.follow.setVisibility(View.GONE);
                binding.unfollow.setVisibility(View.VISIBLE);
                model.followOrUnfollw(masterId);
            });
        }
        // 返回
        binding.back.setOnClickListener(l -> finish());
    }

    void play() {
        String hls = hlsEndopint + roomId + ".m3u8";
        String rtmp = rtmpEndopint + roomId;
        LogKit.p(hls, rtmp);
        player = new PlayerHolder(this, binding.surfaceView, PlayerHolder.Scale.scaleX);
        player.setOnPlayerListener(new PlayerHolder.OnPlayerListener() {
            @Override
            public void onStart(IjkMediaPlayer ijkMediaPlayer) {
                binding.videoLoading.setVisibility(View.GONE);
            }

            @Override
            public void onPlayComplete(IjkMediaPlayer ijkMediaPlayer) {

            }
        });
        player.playAsync(hls);
    }


}