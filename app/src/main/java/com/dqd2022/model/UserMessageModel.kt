package com.dqd2022.model

import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dqd2022.constant.ChatType
import com.dqd2022.dto.MessageDto
import com.dqd2022.dto.StorageInsertUserMessageDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import kit.LogKit
import kit.TimeKit

var userMessageTable = "usermessageentity"

class UserMessageModel {
    var table = userMessageTable
    private val db: SQLiteDatabase

    init {
        db = App.getDb()
    }


    // tiem 和 server_msgid 在服务端返回时间后会更新
    fun insert(dto: StorageInsertUserMessageDto) {
        // 会话编号使用数值相加，查的时候不管是对方发给我还是我发给对方的都能查出来
        val chatNo = (dto.fromUser + dto.toUser).toString()
        val time = TimeKit.nowMillis()
        val sql = "INSERT INTO " + table + " (local_msgid, server_msgid, chatno, belong, type, server_time, message, fromuser, touser)" +
                " VALUES ('" + dto.localMsgId + "','" + dto.serverMsgId + "','" + chatNo + "','" + dto.belong + "','" + dto.msgType + "','" +
                time + "','" + dto.message + "','" + dto.fromUser + "','" + dto.toUser + "')"
        try {
            db.execSQL(sql)
        } catch (e: SQLException) {
            LogKit.p("[StoreInsertUserMessage Error]", e.message)
            e.printStackTrace()
        }
    }

    // 更新消息时间
    fun updateMessageTime(localMsgid: String, serverMsgid: String, time: Long) {
        val sql = "UPDATE $table SET server_time = ?, server_msgid = '${serverMsgid}' WHERE local_msgid = ?"
        try {
            db.execSQL(sql, arrayOf(time.toString(), localMsgid))
            LogKit.p("更新消息时间：", sql)
        } catch (e: SQLException) {
            LogKit.p("[updateMessageTime Error]", e.message)
            e.printStackTrace()
        }
    }

    val lastMsgTime: Long
        // 获取收到的最后一条消息的时间
        get() {
            val sql = "SELECT server_time FROM $table WHERE belong = ? ORDER BY server_time DESC LIMIT 1"
            try {
                val cursor = db.rawQuery(sql, arrayOf(App.myUserId.toString()))
                if (cursor.count > 0) {
                    cursor.moveToNext()
                    val time = cursor.getLong(0)
                    return time
                }
            } catch (e: SQLException) {
                LogKit.p("[getLastMsgTime Error]", e.message)
            }
            return 0L
        }

    // 插入单条消息
    fun insertOne(serverMsgId: String, msgType: Int, time: Long, content: String, fromUser: Int): Boolean {
        // 会话编号使用数值相加，查的时候不管是对方发给我还是我发给对方的都能查出来
        val chatNo = fromUser + App.myUserId
        val sql = "INSERT INTO " + table + "(server_msgid, chatno, belong, type, server_time, message, fromuser, touser) " +
                "VALUES(?,?,?,?,?,?,?,?)"
        return try {
            db.execSQL(
                sql, arrayOf(
                    serverMsgId, chatNo.toString(), App.myUserId.toString(), msgType.toString(), time.toString(),
                    content, fromUser.toString(), App.myUserId.toString()
                )
            )
            true
        } catch (e: SQLException) {
            LogKit.p("[插入单条私聊消息失败]", "msgId:" + serverMsgId, e.message)
            false
        }
    }

    // 获取一页消息
    fun getList(userId: Int, page: Int, rows: Int): MutableList<MessageDto> {
        val list: MutableList<MessageDto> = ArrayList()
        val skip = (page - 1) * rows
        val chatNo = App.myUserId + userId
        val sql = "SELECT t.id, t.server_msgid, t.local_msgid, t.server_time, t.type, t.fromuser, t.message, c.nickname, c.avatar " +
                "FROM $table t LEFT JOIN contactsentity c ON t.fromuser = c.bizid AND c.belong = ${App.myUserId} " +
                " WHERE chatno= $chatNo ORDER BY server_time DESC LIMIT $skip, $rows"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count == 0) {
            return list
        }
        while (cursor.moveToNext()) {
            val item = makeResult(cursor)
            list.add(item)
        }
        cursor.close()
        return list
    }

    fun makeResult(cursor: Cursor): MessageDto {
        val item = MessageDto()
        var avatar: String = ""
        var nick: String = ""
        val idIndex = cursor.getColumnIndex("id")
        var id = cursor.getLong(idIndex)
        val messageIndex = cursor.getColumnIndex("message")
        var message = cursor.getString(messageIndex)
        val avatarIndex = cursor.getColumnIndex("avatar")
        if (avatarIndex != null && avatarIndex > 0) avatar = cursor.getString(avatarIndex)
        val nickIndex = cursor.getColumnIndex("nickname")
        if (avatarIndex != null && avatarIndex > 0) nick = cursor.getString(nickIndex)
        val timeIndex = cursor.getColumnIndex("server_time")
        val time = cursor.getString(timeIndex)
        val localMsgIdIndex = cursor.getColumnIndex("local_msgid")
        val localMsgId = cursor.getString(localMsgIdIndex)
        val msgIdIndex = cursor.getColumnIndex("server_msgid")
        val msgId = cursor.getString(msgIdIndex)
        val fromUserIndex = cursor.getColumnIndex("fromuser")
        val fromUser = cursor.getString(fromUserIndex)
        val msgTypeIndex = cursor.getColumnIndex("type")
        val msgType = cursor.getInt(msgTypeIndex)
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
        return item
    }

    // 清空与某人聊天记录
    fun clearChatRecords(userId: Int) {
        val chatNo = userId + App.myUserId
        val sql = "DELETE FROM $table WHERE chatno = $chatNo"
        db.execSQL(sql)
        val chatId = ImHelpers.makeChatId(ChatType.Private, userId.toString())
        ChatlistTable().delete(chatId)
    }
}