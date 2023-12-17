package com.dqd2022.api;

import com.dqd2022.Config;
import com.dqd2022.dto.AddMeListRes;
import com.dqd2022.dto.AppApiDto;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.dto.ContactsListRes;
import com.dqd2022.dto.RoomListRes;
import com.dqd2022.dto.SyncPrivateMessageRes;
import com.dqd2022.dto.SyncRoomMessageRes;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.dto.UserinfoRes;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ImbizApi {
    Retrofit retrofit;
    private static ImbizApi instance;

    public ImbizApi() {
        retrofit = new RetrofitBuilder(Config.IMBIZ).builder();
    }

    public static ImbizApi getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new ImbizApi();
        return instance;
    }


    // 登录
    public interface LoginApi {
        @POST("/user/login")
        Call<UserApiJavaDto.LoginRegisterRes> apply(@Body UserApiJavaDto.LoginReq dto);
    }

    public Call<UserApiJavaDto.LoginRegisterRes> login(UserApiJavaDto.LoginReq dto) {
        LoginApi api = retrofit.create(LoginApi.class);
        Call<UserApiJavaDto.LoginRegisterRes> call = api.apply(dto);
        return call;
    }

    // 微软云上传凭证
    public interface AzureUpKeyApi {
        @POST("/obstore/azure_blob")
        Call<AppApiDto.AzureUpKey> apply(@Body AppApiDto.AzureUpKeyReq arg1);
    }

    public Call<AppApiDto.AzureUpKey> azureUpKey(int type) {
        AzureUpKeyApi api = retrofit.create(AzureUpKeyApi.class);
        AppApiDto.AzureUpKeyReq req = new AppApiDto.AzureUpKeyReq(type);
        Call<AppApiDto.AzureUpKey> call = api.apply(req);
        return call;
    }

    // 修改用户信息
    public interface UserinfoEditApi {
        @POST("/user/info")
        Call<CommonResDto> apply(@Body UserApiJavaDto.UserinfoEditReq dto);
    }

    public Call<CommonResDto> userinfoEdit(String field, String value) {
        UserinfoEditApi api = retrofit.create(UserinfoEditApi.class);
        UserApiJavaDto.UserinfoEditReq req = new UserApiJavaDto.UserinfoEditReq(field, value);
        Call<CommonResDto> call = api.apply(req);
        return call;
    }

    // 获取用户信息
    public interface GetUserinfoApi {
        @GET("/user/info")
        Call<UserinfoRes> apply(@Query("UserId") String arg1);
    }

    public Call<UserinfoRes> getUserinfo(String userId) {
        GetUserinfoApi api = retrofit.create(GetUserinfoApi.class);
        Call<UserinfoRes> call = api.apply(userId);
        return call;
    }

    // 所有私聊联系人列表
    public interface ContactsListApi {
        @GET("/contacts/list")
        Call<ContactsListRes> apply();
    }

    public Call<ContactsListRes> getContactsList() {
        ContactsListApi api = retrofit.create(ContactsListApi.class);
        Call<ContactsListRes> call = api.apply();
        return call;
    }

    // 所有群聊联系人列表
    public interface RoomListApi {
        @GET("/room/list")
        Call<RoomListRes> apply();
    }

    public Call<RoomListRes> getRoomList() {
        RoomListApi api = retrofit.create(RoomListApi.class);
        Call<RoomListRes> call = api.apply();
        return call;
    }

    // 请求加我为好友的人
    public interface AddMeListApi {
        @GET("/chat/addme-list")
        Call<AddMeListRes> apply();
    }

    public Call<AddMeListRes> getAddMeList() {
        AddMeListApi api = retrofit.create(AddMeListApi.class);
        Call<AddMeListRes> call = api.apply();
        return call;
    }

    // 同步私聊消息
    public interface SyncPrivateMessageApi {
        @GET("/chat/sync-private-message")
        Call<SyncPrivateMessageRes> apply(@Query("LastMessageTime") Long arg1);
    }

    public Call<SyncPrivateMessageRes> syncPrivateMessage(Long lastMessageTime) {
        SyncPrivateMessageApi api = retrofit.create(SyncPrivateMessageApi.class);
        Call<SyncPrivateMessageRes> call = api.apply(lastMessageTime);
        return call;
    }

    // 同步群消息
    public interface SyncRoomMessageApi {
        @GET("/chat/room-msg")
        Call<SyncRoomMessageRes> apply(@Query("RoomId") int arg1, @Query("LastMessageTime") Long arg2);
    }

    public Call<SyncRoomMessageRes> syncRoomMessage(int roomId, Long lastMessageTime) {
        SyncRoomMessageApi api = retrofit.create(SyncRoomMessageApi.class);
        Call<SyncRoomMessageRes> call = api.apply(roomId, lastMessageTime);
        return call;
    }
}
