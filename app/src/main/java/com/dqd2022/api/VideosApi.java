package com.dqd2022.api;

import com.dqd2022.Config;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.dto.VideoApiDto;
import com.dqd2022.dto.VideoHomeListDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class VideosApi {
    Retrofit retrofit;
    private static VideosApi instance;

    public VideosApi() {
        retrofit = new RetrofitBuilder(Config.VideoApi).builder();
    }

    // 单例
    public static VideosApi getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new VideosApi();
        return instance;
    }

    // 定义接口
    public interface HomeVideoService {
        @GET("video/home")
        Call<HomeVideoResponse> videoHomeList();
    }

    // 定义后端返回的数据结构类，不一定要 javabean，只要为返回字段定义属性就行
    public class HomeVideoResponse {
        public int ApiCode;
        public String ApiMessage;
        public String Endpoint;
        public List<VideoHomeListDto> List;
    }

    // 构建请求器
    public Call<HomeVideoResponse> homeList() {
        HomeVideoService service = retrofit.create(HomeVideoService.class);
        Call<HomeVideoResponse> call = service.videoHomeList();
        return call;
    }


    // 用户创建的视频
    public interface UserCrationVideos {
        @GET("/user/creation-videos")
        Call<VideoApiDto.UserVideosResponse> apply(@Query("userId") int arg1, @Query("page") int arg2);
    }

    public Call<VideoApiDto.UserVideosResponse> userCrationVideos(int userId, int page) {
        VideosApi.UserCrationVideos service = retrofit.create(VideosApi.UserCrationVideos.class);
        Call<VideoApiDto.UserVideosResponse> call = service.apply(userId, page);
        return call;
    }

    // 用点赞的视频
    public interface UserSupportVideos {
        @GET("/user/support-videos")
        Call<VideoApiDto.UserVideosResponse> apply(@Query("userId") int arg1, @Query("page") int arg2);
    }

    public Call<VideoApiDto.UserVideosResponse> userSupportVideos(int userId, int page) {
        VideosApi.UserSupportVideos service = retrofit.create(VideosApi.UserSupportVideos.class);
        Call<VideoApiDto.UserVideosResponse> call = service.apply(userId, page);
        return call;
    }

    // 用户收藏的视频
    public interface UserCollectVideos {
        @GET("/user/collect-videos")
        Call<VideoApiDto.UserVideosResponse> apply(@Query("userId") int arg1, @Query("page") int arg2);
    }

    public Call<VideoApiDto.UserVideosResponse> userCollectVideos(int userId, int page) {
        VideosApi.UserCollectVideos service = retrofit.create(VideosApi.UserCollectVideos.class);
        Call<VideoApiDto.UserVideosResponse> call = service.apply(userId, page);
        return call;
    }

    // 点赞/取消点赞
    public interface SupportApi {
        @FormUrlEncoded
        @POST("/video/support")
        Call<CommonResDto> apply(@Field("videoId") int videoId, @Field("action") int action);
    }

    // action: 1.点赞 0.取消点赞
    public Call<CommonResDto> support(int videoId, int action) {
        SupportApi api = retrofit.create(SupportApi.class);
        Call<CommonResDto> call = api.apply(videoId, action);
        return call;
    }

    // 收藏/取消收藏
    public interface CollectApi {
        @FormUrlEncoded
        @POST("/video/collect")
        Call<CommonResDto> apply(@Field("videoId") int videoId, @Field("action") int action);
    }

    // action: 1.收藏 0.取消收藏
    public Call<CommonResDto> collect(int videoId, int action) {
        CollectApi api = retrofit.create(CollectApi.class);
        Call<CommonResDto> call = api.apply(videoId, action);
        return call;
    }

    // 增加转发数
    public interface IncrShareNumApi {
        @FormUrlEncoded
        @POST("/video/incr-share-count")
        Call<CommonResDto> apply(@Field("videoId") int videoId);
    }

    public Call<CommonResDto> incrShareNum(int videoId) {
        IncrShareNumApi api = retrofit.create(IncrShareNumApi.class);
        Call<CommonResDto> call = api.apply(videoId);
        return call;
    }


    // 获取视频信息
    public interface VideoInfoApi {
        @GET("/video/info")
        Call<VideoApiDto.VideoInfoResDto> apply(@Query("videoId") int arg1);
    }

    public Call<VideoApiDto.VideoInfoResDto> getVideoInfo(int videoId) {
        VideoInfoApi api = retrofit.create(VideoInfoApi.class);
        Call<VideoApiDto.VideoInfoResDto> call = api.apply(videoId);
        return call;
    }
}
