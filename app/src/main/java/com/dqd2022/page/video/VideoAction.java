package com.dqd2022.page.video;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import com.dqd2022.R;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.dto.VideoPlaylistItemDto;
import com.dqd2022.helpers.App;
import com.dqd2022.page.login.LoginActivity;

import kit.ImageKit;
import kit.LogKit;
import kit.MmkvKit;
import kit.NumberKit;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoAction {
    View view;
    Activity activity;
    VideoModel model;
    int supportNum, collectNum;

    VideoAction(Activity activity, View view, VideoModel model) {
        this.view = view;
        this.activity = activity;
        this.model = model;
    }

    // 关注用户
    void follow(ImageView img, int userId) {
        view.findViewById(R.id.video_home_follow).setOnClickListener((l) -> {
            if (!App.isLogin()) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                return;
            }
            img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_follow_success));
            model.follow(userId);
            new android.os.Handler().postDelayed(() -> {
                img.setVisibility(View.INVISIBLE);
            }, 2000);
        });
    }

    // 点赞
    void support(ImageView img, int videoId, int supportNum) {
        this.supportNum = supportNum;
        view.findViewById(R.id.video_home_support).setOnClickListener(l -> {
            if (!App.isLogin()) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                return;
            }
            String key = MMKVkey.supportVideos.name();
            if (!MmkvKit.HashSetContains(key, videoId)) {
                // 点赞
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_supported));
                MmkvKit.HashSetAdd(key, videoId);
                model.support(videoId, 1);
                this.supportNum++;
            } else {
                // 取消
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_support));
                MmkvKit.HashSetRemoveItem(key, videoId);
                model.support(videoId, 0);
                this.supportNum--;
            }
            TextView tx = view.findViewById(R.id.video_home_support_count);
            tx.setText(NumberKit.formatWithUnit(App.language, this.supportNum));
        });
    }

    // 收藏
    void collect(ImageView img, int videoId, int collectNum) {
        this.collectNum = collectNum;
        view.findViewById(R.id.video_home_collect).setOnClickListener(l -> {
            if (!App.isLogin()) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                return;
            }
            String key = MMKVkey.collectVideos.name();
            if (!MmkvKit.HashSetContains(key, videoId)) {
                // 收藏
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_collected));
                MmkvKit.HashSetAdd(key, videoId);
                model.collect(videoId, 1);
                this.collectNum++;
            } else {
                // 取消
                img.setImageURI(ImageKit.drawable2uri(activity, R.drawable.home_video_collect));
                MmkvKit.HashSetRemoveItem(key, videoId);
                model.collect(videoId, 0);
                this.collectNum--;
            }
            TextView tx = view.findViewById(R.id.video_home_collect_count);
            tx.setText(NumberKit.formatWithUnit(App.language, this.collectNum));
        });
    }

    // 分享
    void share(ActivityResultLauncher resultLauncher, VideoPlaylistItemDto item, Long progress) {
        view.findViewById(R.id.video_home_share).setOnClickListener(l -> {
            if (!App.isLogin()) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                return;
            }
            Intent i = new Intent(activity, ShareToContactsActivity.class);
            i.putExtra("videoId", item.getVideoId());
            i.putExtra("cover", item.getCover());
            i.putExtra("width", item.getWidth());
            i.putExtra("height", item.getHeight());
            i.putExtra("uri", item.getVideoUri());
            i.putExtra("progress", progress);
            resultLauncher.launch(i);
        });
    }
}
