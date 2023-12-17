package com.dqd2022.page.im.chatting

import com.dqd2022.constant.ChatType
import com.dqd2022.dto.ContactsItemDto
import com.dqd2022.dto.MessageDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.SQLite
import com.dqd2022.model.ContactsModel
import com.dqd2022.model.RoomMemberModel
import com.dqd2022.model.UserMessageModel
import com.dqd2022.model.userMessageTable
import com.dqd2022.storage.roomMessageTable
import kit.LogKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ChattingModel {
    var chatType: Int = 0

    constructor(chatType: Int) {
        this.chatType = chatType
    }


    // 获取联系人信息
    fun getContactsInfo(bizId: Int, callback: (ContactsItemDto) -> Unit) {
        val contactsInfo = ContactsModel().getOne(chatType, bizId)
        if (contactsInfo != null) callback(contactsInfo)
    }

    // 从数据库中删除一条消息
    fun deleteMsg(localMsgId: String?, serverMsgid: String?, roomId: Int?) {
        var sql = ""
        if (chatType == ChatType.Private) {
            if (localMsgId != null) {
                sql = "DELETE FROM $userMessageTable WHERE belong = ${App.myUserId} AND local_msgid = '${localMsgId}'"
            } else if (serverMsgid != null) {
                sql = "DELETE FROM $userMessageTable WHERE belong = ${App.myUserId} AND server_msgid = '${serverMsgid}'"
            }
        } else {
            if (localMsgId != null) {
                sql = "DELETE FROM $roomMessageTable WHERE roomid = $roomId AND local_msgid = '${localMsgId}'"
            } else if (serverMsgid != null) {
                sql = "DELETE FROM $roomMessageTable WHERE roomid = $roomId AND server_msgid = '${serverMsgid}'"
            }
        }
        LogKit.p(sql)
        App.getDb().execSQL(sql)
    }

    // 从数据库中软删除一条消息
    fun softDeleteMsg(localMsgId: String?, serverMsgid: String?) {
        var sql = ""
        if (chatType == ChatType.Private) {
            if (localMsgId != null) {
                sql = "UPDATE $userMessageTable SET is_delete = 1 WHERE belong = ${App.myUserId} AND local_msgid = '${localMsgId}'"
            } else if (serverMsgid != null) {
                sql = "UPDATE $userMessageTable SET is_delete = 1 WHERE belong = ${App.myUserId} AND server_msgid = '${serverMsgid}'"
            }
        } else {
            if (localMsgId != null) {
                sql = "UPDATE $roomMessageTable SET is_delete = 1 WHERE belong = ${App.myUserId} AND local_msgid = '${localMsgId}'"
            } else if (serverMsgid != null) {
                sql = "UPDATE $roomMessageTable SET is_delete = 1  WHERE belong = ${App.myUserId} AND server_msgid = '${serverMsgid}'"
            }
        }
        App.getDb().execSQL(sql)
    }

    // 根据 localMsgId 获取 serverMsgId
    fun getServerMsgIdByLocalMsgId(localMsgId: String, roomId: Int): String {
        var sql: String
        if (chatType == ChatType.Private) {
            sql = "SELECT server_msgid FROM $userMessageTable WHERE belong = ${App.myUserId} AND local_msgid = '${localMsgId}'"
        } else {
            sql = "SELECT server_msgid FROM $roomMessageTable WHERE roomid = ${roomId} AND local_msgid = '${localMsgId}'"
        }
        return SQLite().getOne(sql).getFieldToString("server_msgid")
    }

    fun getItemByServerId(serverMsgid: String): MessageDto? {
        var sql: String
        if (chatType == ChatType.Private) {
            sql = "SELECT * FROM $userMessageTable WHERE belong = ${App.myUserId} AND server_msgid = '${serverMsgid}'"
        } else {
            sql = "SELECT * FROM $roomMessageTable WHERE belong = ${App.myUserId} AND local_msgid = '${serverMsgid}'"
        }
        val cursor = App.getDb().rawQuery(sql, null)
        if (cursor.count == 0) {
            return null
        }
        cursor.moveToNext()
        val ret = UserMessageModel().makeResult(cursor)
        return ret
    }

}