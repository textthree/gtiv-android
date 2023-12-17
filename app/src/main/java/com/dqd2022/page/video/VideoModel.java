package com.dqd2022.page.video;

import android.content.Context;

import com.dqd2022.api.UsersApi;
import com.dqd2022.api.VideosApi;
import com.dqd2022.constant.CachePath;
import com.dqd2022.dto.CommonResDto;

import kit.FileKit;
import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoModel {
    VideoModel() {
    }


    // 获取视频播放地址，如果有缓存则用本地缓存
    String getVideoUri(Context context, String videoUrl) {
        String[] seg = videoUrl.split("/");
        String dir = CachePath.video(context) + seg[seg.length - 2] + "/";
        if (FileKit.exists(dir + "cached")) {
            String local = dir + "index.m3u8";
            //LogKit.p("使用本地缓存资源进行播放：", local);
            return local;
        }
        return videoUrl;
    }


    // 关注/取消关注
    void follow(int userId) {
        UsersApi.getInstance().follow(userId).enqueue(new Callback<CommonResDto>() {
            @Override
            public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {

            }

            @Override
            public void onFailure(Call<CommonResDto> call, Throwable t) {

            }
        });
    }

    // 点赞/取消点赞
    void support(int videoId, int action) {
        VideosApi.getInstance().support(videoId, action).enqueue(new Callback<CommonResDto>() {
            @Override
            public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {

            }

            @Override
            public void onFailure(Call<CommonResDto> call, Throwable t) {

            }
        });
    }

    // 收藏/取消收藏
    void collect(int videoId, int action) {
        VideosApi.getInstance().collect(videoId, action).enqueue(new Callback<CommonResDto>() {
            @Override
            public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {

            }

            @Override
            public void onFailure(Call<CommonResDto> call, Throwable t) {

            }
        });
    }


}
