package com.dqd2022.model

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dqd2022.R
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.dto.MessageDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.storage.roomMemberTable
import com.dqd2022.storage.roomMessageTable
import kit.LogKit
import kit.TimeKit


class RoomMessageTable {
    var table = roomMessageTable
    private val db: SQLiteDatabase

    init {
        db = App.getDb()
    }

    class StorageInsertRoomMessageDto {
        @JvmField
        var roomId = 0

        @JvmField
        var localMsgId = ""

        @JvmField
        var serverMsgId = ""

        @JvmField
        var msgType = 0

        @JvmField
        var fromUser = ""

        @JvmField
        var message = ""
    }

    fun insertOne(roomId: Int, fromUser: Int, msgId: String, msgType: Int, message: String) {
        val item = StorageInsertRoomMessageDto()
        item.roomId = roomId
        item.serverMsgId = msgId
        item.msgType = msgType
        item.fromUser = fromUser.toString()
        item.message = message
        insert(item)
    }

    fun insert(dto: StorageInsertRoomMessageDto) {
        val time = TimeKit.nowMillis() // 服务端返回时间后会更新
        val sql = "INSERT INTO " + table + " (local_msgid, server_msgid, roomid, type, server_time, message, fromuser) " +
                "VALUES ('${dto.localMsgId}', '${dto.serverMsgId}', ${dto.roomId}, ${dto.msgType}, $time, '${dto.message}', '${dto.fromUser}')"
        try {
            db.execSQL(sql)
        } catch (e: SQLException) {
            LogKit.p("[StoreInsertRoomMessage Error]", e.message)
            e.printStackTrace()
        }
    }

    // 更新消息时间
    fun updateMessageTime(localMsgid: String, serverMsgid: String, time: Long) {
        val sql = "UPDATE $table SET server_time = '${time}' , server_msgid ='${serverMsgid}' WHERE local_msgid = '${localMsgid}'"
        try {
            db.execSQL(sql)
            LogKit.p("更新消息时间：", sql)
        } catch (e: SQLException) {
            LogKit.p("[updateRoomMessageTime Error]", e.message)
            e.printStackTrace()
        }
    }

    // 获取一页消息，按发送方发送消息时间倒序
    fun getList(roomId: Int, page: Int, rows: Int): MutableList<MessageDto> {
        val list: MutableList<MessageDto> = ArrayList()
        val skip = (page - 1) * rows
        val sql = "SELECT t.id, t.local_msgid, t.server_msgid, t.server_time, t.type, t.fromuser, t.message, t.sender_quited, " +
                " m.nickname, m.avatar " +
                "FROM $table t LEFT JOIN $roomMemberTable m ON t.fromuser = m.user_id AND m.room_id = $roomId " +
                " WHERE t.roomid= $roomId ORDER BY t.server_time DESC LIMIT $skip, $rows"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count == 0) {
            return list
        }
        while (cursor.moveToNext()) {
            val item = MessageDto()
            val idIndex = cursor.getColumnIndex("id")
            var id = cursor.getLong(idIndex)
            val messageIndex = cursor.getColumnIndex("message")
            var message = cursor.getString(messageIndex)
            val avatarIndex = cursor.getColumnIndex("avatar")
            val avatar = cursor.getString(avatarIndex)
            val nickIndex = cursor.getColumnIndex("nickname")
            val nick = cursor.getString(nickIndex)
            val timeIndex = cursor.getColumnIndex("server_time")
            val localMsgIdIndex = cursor.getColumnIndex("local_msgid")
            val localMsgId = cursor.getString(localMsgIdIndex)
            val time = cursor.getString(timeIndex)
            val msgIdIndex = cursor.getColumnIndex("server_msgid")
            val msgId = cursor.getString(msgIdIndex)
            val fromUserIndex = cursor.getColumnIndex("fromuser")
            val fromUser = cursor.getString(fromUserIndex)
            val msgTypeIndex = cursor.getColumnIndex("type")
            val msgType = cursor.getInt(msgTypeIndex)
            val senderQuitedIndex = cursor.getColumnIndex("sender_quited")
            val senderQuited = cursor.getInt(senderQuitedIndex)
            if (msgType == MessageType.Repeal) {
                message = App.context.getString(R.string.RepealMessage)
            }
            item.id = id
            item.content = message
            item.nickname = nick
            item.avatar = avatar
            if (time != null) item.setTime(App.language, time)
            item.msgType = msgType
            item.serverMsgId = msgId
            item.localMsgId = localMsgId
            item.fromUserId = fromUser
            item.isMine = fromUser.toInt() == App.myUserId
            item.senderQuited = senderQuited == 1
            list.add(item)
        }
        cursor.close()
        return list
    }

    // 清空与某群聊天记录
    // FIXME: 这里有个 bug，因为群消息表没有 belong 字段，所以如果一台手机上登过两个号，其中一个号退群，另一个号也没记录了
    fun clearChatRecords(roomId: Int) {
        val sql = "DELETE FROM $table WHERE roomid = $roomId"
        db.execSQL(sql)
    }
}