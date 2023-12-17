package com.dqd2022.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.Config;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.dto.MyUserListRes;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.dto.UserinfoDto;

import kit.HttpKit;
import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class UsersApi {
    public Retrofit retrofit;
    private static UsersApi instance;

    public UsersApi() {
        retrofit = new RetrofitBuilder(Config.VideoApi).builder();
    }

    public static UsersApi getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new UsersApi();
        return instance;
    }

    // 去服务器获取头像和昵称
    public static UserinfoDto getUserinfo(String fromUserId) {
        UserinfoDto ret = new UserinfoDto();
        try {
            String url = Config.IMBIZ + "/user/info?UserId=" + fromUserId;
            String res = new HttpKit().get(url, Base.getHeadParams());
            JSONObject resObj = JSON.parseObject(res);
            String avatar = resObj.getString("Avatar");
            String nick = resObj.getString("Nickname");
            int gender = resObj.getInteger("Gender");
            String username = resObj.getString("Username");
            if (!avatar.equals("")) {
                ret.avatar = avatar;
            }
            if (!nick.equals("")) {
                ret.nickname = nick;
            }
            if (!username.equals("")) {
                ret.username = username;
            }
            ret.gender = gender;
        } catch (Exception e) {
            LogKit.p("获取用户信息失败" + e);
            e.printStackTrace();
        }
        return ret;
    }

    public static UserinfoDto getUserinfo(int fromUserId) {
        return getUserinfo(String.valueOf(fromUserId));
    }

    // 播主信息
    public interface VideoMasterService {
        @GET("/user/video-master-info")
        Call<UserApiJavaDto.VideoMasterResponse> videoMasterInfo(@Query("userId") String arg1);
    }

    public Call<UserApiJavaDto.VideoMasterResponse> videoMaster(String userId) {
        VideoMasterService service = retrofit.create(VideoMasterService.class);
        Call<UserApiJavaDto.VideoMasterResponse> call = service.videoMasterInfo(userId);
        return call;
    }

    // 关注
    public interface FollowApi {
        @GET("/user/follow")
        Call<CommonResDto> apply(@Query("userId") int arg1);
    }

    public Call<CommonResDto> follow(int userId) {
        FollowApi api = retrofit.create(FollowApi.class);
        Call<CommonResDto> call = api.apply(userId);
        return call;
    }

    // 账号密码注册
    public interface RegisterByUsernameApi {
        @FormUrlEncoded
        @POST("/user/register-by-username")
        Call<UserApiJavaDto.LoginRegisterRes> apply(@Field("username") String arg1, @Field("password") String arg2);
    }

    public Call<UserApiJavaDto.LoginRegisterRes> registerByUsername(String username, String password) {
        RegisterByUsernameApi api = retrofit.create(RegisterByUsernameApi.class);
        Call<UserApiJavaDto.LoginRegisterRes> call = api.apply(username, password);
        return call;
    }

    // 我关注的用户列表
    public interface MyFollowListApi {
        @GET("/user/follow-list")
        Call<MyUserListRes> apply(@Query("page") int arg1);
    }

    public interface MyUserListCallback {
        void apply(MyUserListRes response);
    }

    public Call<MyUserListRes> myFollowList(int page, MyUserListCallback calback) {
        MyFollowListApi api = retrofit.create(MyFollowListApi.class);
        Call<MyUserListRes> call = api.apply(page);
        call.enqueue(new Callback<MyUserListRes>() {
            @Override
            public void onResponse(Call<MyUserListRes> call, Response<MyUserListRes> response) {
                if (!response.isSuccessful()) return;
                MyUserListRes res = response.body();
                if (res.ApiCode != 0) return;
                calback.apply(res);
            }

            @Override
            public void onFailure(Call<MyUserListRes> call, Throwable t) {
            }
        });
        return call;
    }

    // 我的粉丝列表
    public interface MyFansListApi {
        @GET("/user/fans-list")
        Call<MyUserListRes> apply(@Query("page") int arg1);
    }

    public Call<MyUserListRes> myFansList(int page, MyUserListCallback calback) {
        MyFansListApi api = retrofit.create(MyFansListApi.class);
        Call<MyUserListRes> call = api.apply(page);
        call.enqueue(new Callback<MyUserListRes>() {
            @Override
            public void onResponse(Call<MyUserListRes> call, Response<MyUserListRes> response) {
                if (!response.isSuccessful()) return;
                MyUserListRes res = response.body();
                if (res.ApiCode != 0) return;
                calback.apply(res);
            }

            @Override
            public void onFailure(Call<MyUserListRes> call, Throwable t) {
            }
        });
        return call;
    }


    // 我的获赞列表
    public interface MySupportListApi {
        @GET("/user/support-list")
        Call<MyUserListRes> apply(@Query("page") int arg1);
    }

    public Call<MyUserListRes> mySupportList(int page, MyUserListCallback calback) {
        MySupportListApi api = retrofit.create(MySupportListApi.class);
        Call<MyUserListRes> call = api.apply(page);
        call.enqueue(new Callback<MyUserListRes>() {
            @Override
            public void onResponse(Call<MyUserListRes> call, Response<MyUserListRes> response) {
                if (!response.isSuccessful()) return;
                MyUserListRes res = response.body();
                if (res.ApiCode != 0) return;
                calback.apply(res);
            }

            @Override
            public void onFailure(Call<MyUserListRes> call, Throwable t) {
            }
        });
        return call;
    }


}
