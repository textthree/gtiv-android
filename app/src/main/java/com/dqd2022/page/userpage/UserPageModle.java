package com.dqd2022.page.userpage;

import com.dqd2022.api.UsersApi;
import com.dqd2022.api.VideosApi;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.dto.VideoApiDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class UserPageModle {


    interface GetVideoMasterInfoCallback {
        void apply(UserApiJavaDto.VideoMasterResponse response);
    }

    interface GetUserVideosCallback {
        void apply(VideoApiDto.UserVideosResponse response);
    }

    // 获取播主信息
    public void getVideoMasterInfo(GetVideoMasterInfoCallback fn, int userId) {
        UsersApi.getInstance().videoMaster(String.valueOf(userId)).enqueue(new Callback<UserApiJavaDto.VideoMasterResponse>() {
            @Override
            public void onResponse(Call<UserApiJavaDto.VideoMasterResponse> call, Response<UserApiJavaDto.VideoMasterResponse> response) {
                UserApiJavaDto.VideoMasterResponse res = response.body();
                if (res.ApiCode == 0) fn.apply(res);
            }

            @Override
            public void onFailure(Call<UserApiJavaDto.VideoMasterResponse> call, Throwable t) {
            }
        });
    }

    // 获取播主发布的视频
    public void getUserCreationVideos(GetUserVideosCallback fn, int userId, int page) {
        Call<VideoApiDto.UserVideosResponse> api = VideosApi.getInstance().userCrationVideos(userId, page);
        api.enqueue(new Callback<VideoApiDto.UserVideosResponse>() {
            @Override
            public void onResponse(Call<VideoApiDto.UserVideosResponse> call, Response<VideoApiDto.UserVideosResponse> response) {
                VideoApiDto.UserVideosResponse res = response.body();
                if (res.ApiCode == 0) fn.apply(res);
            }

            @Override
            public void onFailure(Call<VideoApiDto.UserVideosResponse> call, Throwable t) {

            }
        });
    }

    // 获取播主点赞的视频
    public void getUserSupportVideos(GetUserVideosCallback fn, int userId, int page) {
        VideosApi.getInstance().userSupportVideos(userId, page).enqueue(new Callback<VideoApiDto.UserVideosResponse>() {
            @Override
            public void onResponse(Call<VideoApiDto.UserVideosResponse> call, Response<VideoApiDto.UserVideosResponse> response) {
                VideoApiDto.UserVideosResponse res = response.body();
                if (res.ApiCode == 0) fn.apply(res);
            }

            @Override
            public void onFailure(Call<VideoApiDto.UserVideosResponse> call, Throwable t) {

            }
        });
    }

//    // 获取播主收藏的视频
//    public void getUserCollectVideos(GetUserVideosCallback fn, int userId) {
//        VideosApi.getInstance().userSupportVideos(userId, 1).enqueue(new Callback<VideoApiDto.UserVideosResponse>() {
//            @Override
//            public void onResponse(Call<VideoApiDto.UserVideosResponse> call, Response<VideoApiDto.UserVideosResponse> response) {
//                VideoApiDto.UserVideosResponse res = response.body();
//                if (res.ApiCode == 0) fn.apply(res);
//            }
//
//            @Override
//            public void onFailure(Call<VideoApiDto.UserVideosResponse> call, Throwable t) {
//
//            }
//        });
//    }
}