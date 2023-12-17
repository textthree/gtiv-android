package com.dqd2022.model

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dqd2022.api.API
import com.dqd2022.api.ImbizApi
import com.dqd2022.constant.ChatType
import com.dqd2022.dto.ContactsItemDto
import com.dqd2022.dto.ContactsListRes
import com.dqd2022.dto.RoomListRes
import com.dqd2022.helpers.App
import com.dqd2022.helpers.CacheHelpers
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.SQLite
import com.dqd2022.interfaces.CallbackVoid
import com.dqd2022.interfaces.CallbackWithStringArg
import com.dqd2022.storage.contactsTable
import com.dqd2022.storage.entity.ContactsEntity
import kit.LogKit
import kit.TimeKit
import org.openapitools.client.models.T3imapiv1RoomInfoRes
import org.openapitools.client.models.T3imapiv1UserinfoRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactsModel {
    var table = contactsTable
    private var db: SQLiteDatabase
    private var context: Context

    constructor(context: Context) {
        db = App.getDb()
        this.context = context
    }

    constructor() {
        db = App.getDb()
        context = App.context
    }

    fun getOne(chatType: Int, bizId: Int): ContactsItemDto? {
        val contactsId = ImHelpers.makeChatId(chatType, bizId.toString())
        return getById(contactsId)
    }

    fun getById(contactsId: String): ContactsItemDto? {
        val sql = "SELECT * FROM $table WHERE chatid = '$contactsId'"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count > 0) {
            cursor.moveToNext()
            return makeResult(cursor)
        }
        cursor.close()
        return null
    }

    private fun makeResult(cursor: Cursor): ContactsItemDto {
        val ret = ContactsItemDto()
        ret.contactsId = SQLite.getStringFromCursor(cursor, "chatid")
        val nickname = SQLite.getStringFromCursor(cursor, "nickname")
        ret.nickname = nickname
        ret.username = SQLite.getStringFromCursor(cursor, "username")
        ret.avatar = SQLite.getStringFromCursor(cursor, "avatar")
        val bizid = SQLite.getIntFromCursor(cursor, "bizid")
        ret.bizId = bizid
        val chatType = SQLite.getIntFromCursor(cursor, "type")
        ret.chatType = chatType
        ret.memberNum = SQLite.getIntFromCursor(cursor, "membernum")
        ret.gender = SQLite.getIntFromCursor(cursor, "gender")
        ret.addTime = SQLite.getIntFromCursor(cursor, "addtime").toLong()
        ret.isDeleted = SQLite.getIntFromCursor(cursor, "isdeleted")
        ret.lastSyncMsgTime = SQLite.getLongFromCursor(cursor, "lastsyncmsgtime")
        ret.state = SQLite.getIntFromCursor(cursor, "state")
        return ret
    }

    // 同步联系人。目前没做增量减量合并，简单粗暴全量 rebuild
    fun syncContacts(callback: CallbackVoid) {
        try {
            reBuildContacts(callback)
        } catch (e: Exception) {
            callback.apply()
        }
        // 将自己作为一条联系人
        saveOnePrivateContacts(
            ImHelpers.makeChatId(ChatType.Private, App.myUserId.toString()), App.myUserId.toString(),  // bizId
            App.myNickname,  // nickname
            App.myUsername,  // username
            "0",  // gender
            App.myAvatar,  // avatar
            "0" // isDeleted
        )
    }

    val contactsNum: Int
        // 获取联系人数量
        get() {
            val sql = "SELECT count(*) count FROM " + table + " WHERE belong = " + App.myUserId
            val cursor = db.rawQuery(sql, null)
            cursor.moveToNext()
            val contactsNum = cursor.getInt(0)
            LogKit.p("SQL 查询联系人数量", contactsNum)
            cursor.close()
            return contactsNum
        }
    val friendNum: Int
        get() {
            val sql = "SELECT count(*) count FROM " + table + " WHERE belong = " + App.myUserId + " AND type = " + ChatType.Private
            val cursor = db.rawQuery(sql, null)
            cursor.moveToNext()
            val contactsNum = cursor.getInt(0)
            cursor.close()
            return contactsNum
        }
    val roomNum: Int
        get() {
            val sql = "SELECT count(*) count FROM " + table + " WHERE type = " + ChatType.Room
            val cursor = db.rawQuery(sql, null)
            cursor.moveToNext()
            val contactsNum = cursor.getInt(0)
            cursor.close()
            return contactsNum
        }

    private fun reBuildContacts(callback: CallbackVoid) {
        val sql = "DELETE FROM " + table + "  WHERE belong = '" + App.myUserId + "'"
        db.execSQL(sql)
        ImbizApi.getInstance().contactsList.enqueue(object : Callback<ContactsListRes?> {
            override fun onResponse(call: Call<ContactsListRes?>, response: Response<ContactsListRes?>) {
                val res = response.body()
                val list = res!!.List
                for (item in list) {
                    val contactsId = ImHelpers.makeChatId(ChatType.Private, item.UserId)
                    // 头像
                    val cache = CacheHelpers.getInstance().downloadAvatar(context, item.Avatar, true)
                    val avatar = cache.localFileUri
                    // 插入
                    saveOnePrivateContacts(
                        contactsId,
                        item.UserId,  // bizId
                        item.Nickname,  // nickname
                        item.Username, item.Gender.toString(),  // gender
                        avatar, item.Deleted.toString()
                    )
                }
                LogKit.p("同步拉取私聊联系人完成，拉取数量：", list.size)
                syncRommMessage(callback)
            }

            override fun onFailure(call: Call<ContactsListRes?>, t: Throwable) {
                LogKit.p("[拉取私聊联系人失败]")
                syncRommMessage(callback)
            }
        })
    }

    // 同步群聊联系人
    fun syncRommMessage(callback: CallbackVoid) {
        ImbizApi.getInstance().roomList.enqueue(object : Callback<RoomListRes?> {
            override fun onResponse(call: Call<RoomListRes?>, response: Response<RoomListRes?>) {
                val res = response.body()
                val list = res!!.List
                for (item in list) {
                    val chatId = ImHelpers.makeChatId(ChatType.Room, item.RoomId)
                    // 头像
                    val cache = CacheHelpers.getInstance().downloadAvatar(context, item.Avatar, true)
                    val avatar = cache.localFileUri
                    val sql = "INSERT INTO " + table + " (chatid, bizid, belong, type, nickname, avatar, addtime, membernum) VALUES " +
                            "(?, ?, ?, ?, ?, ?, ?, ?)"
                    db.execSQL(
                        sql, arrayOf(
                            chatId,  // id
                            item.RoomId, App.myUserId.toString(), ChatType.Room.toString(),  // type
                            item.RoomName,  // nickname
                            avatar, TimeKit.nowSecond().toString(), item.MemberNum
                        )
                    )
                }
                LogKit.p("同步拉取群聊联系人完成")
                callback.apply()
            }

            override fun onFailure(call: Call<RoomListRes?>, t: Throwable) {
                LogKit.p("[拉取群列表失败]")
                callback.apply()
            }
        })
    }

    // 插入一条私聊联系人
    fun saveOnePrivateContacts(
        contactsId: String,
        bizId: String,
        nickname: String,
        username: String,
        gender: String,
        avatar: String,
        deleted: String
    ) {
        // SQL
        var sql = "SELECT id FROM $table WHERE chatid = '$contactsId'"
        val rows = db.rawQuery(sql, null)
        if (rows.count == 0) {
            sql = "INSERT INTO $table(chatid, bizid, belong, type, nickname, username, gender, avatar, addtime, isdeleted) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            try {
                db.execSQL(
                    sql, arrayOf(
                        contactsId,  // id
                        bizId, App.myUserId.toString(), ChatType.Private.toString(),  // type
                        nickname,  // nickname
                        username,  // username
                        gender,  // gender
                        avatar, TimeKit.nowSecond().toString(),  // addTime
                        deleted
                    )
                )
            } catch (e: SQLException) {
                LogKit.p("[插入私聊联系人错误]", e.message)
            }
        } else {
            // 已存在，更新 isdeleted 代表对方删了我又加上了
            sql = "UPDATE $table SET isdeleted = 0, nickname = ?, avatar = ? WHERE chatid = '$contactsId'"
            db.execSQL(sql, arrayOf(nickname, avatar))
            LogKit.p("[联系人已存在] 更新：", nickname, avatar)
        }
    }

    // 从服务端拉取一个群保存到本地
    fun insertRoomById(roomId: String?, vararg callback: CallbackVoid) {
        API().Room.roomInfoGet(roomId!!).enqueue(object : Callback<T3imapiv1RoomInfoRes?> {
            override fun onResponse(call: Call<T3imapiv1RoomInfoRes?>, response: Response<T3imapiv1RoomInfoRes?>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    LogKit.p("群", roomId, res, res?.roomName, res?.id)
                    val contacts = ContactsEntity()
                    contacts.chatId = ImHelpers.makeChatId(ChatType.Room, res!!.id)
                    contacts.bizId = Integer.valueOf(res.id)
                    contacts.belong = App.myUserId
                    contacts.type = ChatType.Room
                    contacts.nickname = res.roomName
                    val cache = CacheHelpers.getInstance().downloadAvatar(res.avatar, false)
                    contacts.avatar = cache.localFileUri
                    contacts.addTime = TimeKit.nowSecond()
                    // 时间往回推一个小时，方便建立连接时能拉到消息
                    contacts.lastSyncMsgTime = TimeKit.nowMillis() - 3600 * 1000
                    val r = contacts.save()
                    if (!r) {
                        LogKit.p("[新群保存失败]")
                    }
                    if (callback.size > 0) callback[0].apply()
                }
            }

            override fun onFailure(call: Call<T3imapiv1RoomInfoRes?>, t: Throwable) {}
        })
    }

    // 获取列表
    fun getContactsList(chatType: Int): Array<ContactsItemDto> {
        var sql = if (chatType == ChatType.Private) {
            "SELECT * FROM $table  WHERE belong = ? AND type = ?"
        } else {
            "SELECT * FROM $table  WHERE belong = ? AND type = ?"
        }
        try {
            val rows = db.rawQuery(sql, arrayOf(App.myUserId.toString(), chatType.toString()))
            var i = 0
            if (rows.count > 0) {
                var ret = Array(rows.count) { ContactsItemDto() }
                while (rows.moveToNext()) {
                    ret[i] = makeResult(rows)
                    i++
                }
                return ret
            }
        } catch (e: SQLException) {
            LogKit.p("[获取群列表错误]", e.message)
        }
        return emptyArray<ContactsItemDto>()
    }


    // 记录消息同步时间，目前主要是群类型的消息需要用到
    fun updateRoomMsgSyncTime(contactsId: String, time: Long) {
        val sql = "UPDATE $table SET lastsyncmsgtime = ? WHERE chatid = ?"
        db.execSQL(sql, arrayOf(time.toString(), contactsId))
    }

    // 改群名称
    fun updateField(contactsId: String, field: String, value: String) {
        val sql = "UPDATE $table SET $field = ? WHERE chatid = ?"
        try {
            db.execSQL(sql, arrayOf(value, contactsId))
        } catch (e: SQLException) {
            LogKit.p("更新 Contacts 字段", field, "失败", e.message)
            e.printStackTrace()
        }
    }

    // 更新好友信息
    fun updateFriendInfo(chatId: String, userId: Int, callback: CallbackWithStringArg) {
        API().User.userInfoGet(userId.toString()).enqueue(object : Callback<T3imapiv1UserinfoRes?> {
            override fun onResponse(call: Call<T3imapiv1UserinfoRes?>, response: Response<T3imapiv1UserinfoRes?>) {
                if (!response.isSuccessful) return
                val res = response.body()
                if (res!!.apiCode == 0) {
                    val entity = ContactsEntity()
                    entity.nickname = res.nickname
                    entity.updateAll("chatid = ?", chatId)
                    callback.apply(entity.nickname)
                }
            }

            override fun onFailure(call: Call<T3imapiv1UserinfoRes?>, t: Throwable) {}
        })
    }

    // 删除联系人
    fun deleteContacts(contactsId: String) {
        val sql = "DELETE FROM $table WHERE chatid = '$contactsId'"
        db.execSQL(sql)
    }

}