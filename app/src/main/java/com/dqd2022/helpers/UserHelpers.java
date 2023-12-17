package com.dqd2022.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dqd2022.api.API;
import com.dqd2022.api.UsersApi;
import com.dqd2022.constant.CachePath;
import com.dqd2022.constant.ChatType;
import com.dqd2022.dto.RoomMemberInfoDto;
import com.dqd2022.dto.UserinfoDto;
import com.dqd2022.model.ContactsModel;
import com.dqd2022.model.RoomMemberModel;

import org.openapitools.client.models.T3imapiv1RoomMemberInfoRes;

import java.io.IOException;

import kit.HttpKit;
import retrofit2.Response;

public class UserHelpers {
    Context ctx;
    private SQLiteDatabase db;

    public UserHelpers(Context context) {
        this.ctx = context;
        this.db = App.getDb();
    }

    public UserHelpers() {
        this.db = App.getDb();
        this.ctx = App.context;
    }


    // 从服务器获取最新用户信息并保存为联系人
    // Fixme： deleted 总是设置为 0 ，这里可能会导致一个潜在的 bug
    public UserinfoDto saveContactsByUserId(int userId) {
        UserinfoDto info = UsersApi.getUserinfo(userId);
        String avatar = "";
        if (info != null && info.avatar != null && !info.avatar.equals("") && info.avatar.startsWith("http")) {
            avatar = new HttpKit().downloadFileAsync(info.avatar, CachePath.storage(this.ctx));
        }
        ContactsModel table = new ContactsModel();
        String contactsId = ImHelpers.makeChatId(ChatType.Private, String.valueOf(userId));
        String bizId = String.valueOf(userId);
        String nickname = info.nickname;
        String username = info.username;
        String gender = String.valueOf(info.gender);
        table.saveOnePrivateContacts(contactsId, bizId, nickname, username, gender, avatar, "0");
        return info;
    }

    // 从服务获取用户新
    public UserinfoDto getUserinfoFromServer(int userId) {
        return UsersApi.getUserinfo(userId);
    }

    // 获取用户信息，如果不存在则从服务器拉取
    public UserinfoDto getFriendInfo(int userId) {
        UserinfoDto ret = new UserinfoDto();
        String sql = "SELECT nickname, avatar FROM contactsentity WHERE type = 1 AND bizId = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        if (cursor.getCount() == 0) {
            ret = UsersApi.getUserinfo(userId);
//            String avatar = "";
//            if (ret != null && ret.avatar != null && !ret.avatar.equals("") && ret.avatar.startsWith("http")) {
//                avatar = new HttpKit().downloadFileAsync(ret.avatar, CachePath.storage(this.ctx));
//            }
//            sql = "INSERT INTO room_member(userId, nickname, avatar, updateTime) " +
//                    "VALUES(" + userId + ", '" + ret.nickname + "', '" + avatar + "', " + new Date().getTime() + ")";
//            db.execSQL(sql);
        } else {
            cursor.moveToNext();
            int avatarIndex = cursor.getColumnIndex("avatar");
            int nickIndex = cursor.getColumnIndex("nickname");
            ret.avatar = cursor.getString(avatarIndex);
            ret.nickname = cursor.getString(nickIndex);
        }
        return ret;
    }


}
