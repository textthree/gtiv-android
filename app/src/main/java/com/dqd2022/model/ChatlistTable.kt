package com.dqd2022.model

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import com.dqd2022.dto.ChatItemDto
import com.dqd2022.dto.DbChatlistItemDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.SQLite
import com.dqd2022.storage.chatlistTable
import com.dqd2022.storage.contactsTable
import kit.LogKit

class ChatlistTable {
    private val db: SQLiteDatabase
    var table = chatlistTable


    init {
        db = App.getDb()
    }

    val list: LinkedHashMap<String, ChatItemDto>
        // 获取所有会话列表。TODO: 分页
        get() {
            val ret = LinkedHashMap<String, ChatItemDto>()
            val sql = "SELECT t.*, c.avatar, c.nickname FROM " + table + " t " +
                    "LEFT JOIN " + contactsTable + " c ON t.chatid = c.chatid " +
                    "WHERE t.belong = '" + App.myUserId + "' " +
                    "ORDER BY lastmessagetime, topTime"
            try {
                val cursor = db.rawQuery(sql, null)
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val item = ChatItemDto()
                        val chatId = SQLite.getStringFromCursor(cursor, "chatid")
                        item.contactsId = chatId
                        var nickname = SQLite.getStringFromCursor(cursor, "nickname")
                        if (nickname == null || nickname == "") {
                            // 临时会话在联系人表中是没有的
                            nickname = SQLite.getStringFromCursor(cursor, "chattitle")
                        }
                        item.name = nickname!!
                        val avatar = SQLite.getStringFromCursor(cursor, "avatar")
                        if (avatar != null && avatar != "") {
                            item.avatar = avatar
                        } else {
                            item.avatar = ""
                        }
                        item.id = SQLite.getLongFromCursor(cursor, "id")
                        item.chatType = SQLite.getIntFromCursor(cursor, "chattype")
                        item.topTime = SQLite.getIntFromCursor(cursor, "toptime")
                        item.badgeNum = SQLite.getIntFromCursor(cursor, "badgenum")
                        item.lastMessageDesc = SQLite.getStringFromCursor(cursor, "lastmessagedesc")
                        item.lastMessageTime = SQLite.getLongFromCursor(cursor, "lastmessagetime")
                        ret[chatId] = item
                    }
                }
                cursor.close()
            } catch (e: SQLException) {
                LogKit.p("[查询会话列表失败]", e.message)
            }
            return ret
        }

    // 保存一条记录，已存在更新，不存在插入，根据主键 repalce into
    fun saveChatItem(dto: DbChatlistItemDto) {
        val badgeNum = dto.badgeNum!!
        if (dto.badgeNum == null) {
            badgeNum == 1
        }
        var sql = "SELECT chatid FROM " + table + " WHERE chatid ='" + dto.chatId + "' LIMIT 1"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count == 0) {
            sql = "INSERT INTO " + table + "(chatid, belong, chattitle, chattype," +
                    "badgenum, lastmessagetime, lastmessagedesc, toptime) VALUES(" + "?,?,?,?,?,?,?,?)"
            try {
                db.execSQL(
                    sql, arrayOf(
                        dto.chatId, App.myUserId.toString(),  // belong
                        dto.title, dto.chatType.toString(), badgeNum.toString(), dto.lastMessageTime.toString(),
                        dto.lastMessageDesc,
                        "0" // topTime
                    )
                )
            } catch (e: SQLException) {
                LogKit.p("[插入新会话失败]", e.message)
            }
        } else {
            sql = "UPDATE " + table + " SET lastmessagedesc = ?, lastmessagetime = ?, badgenum = badgenum + ?, chattitle = ? " +
                    "WHERE chatid = '" + dto.chatId + "'"
            try {
                db.execSQL(
                    sql, arrayOf(
                        dto.lastMessageDesc, dto.lastMessageTime.toString(), badgeNum.toString(),
                        dto.title
                    )
                )
            } catch (e: SQLException) {
                LogKit.p("[更新会话失败]", e.message)
            }
        }
    }

    fun clearBadgeNum(chatId: String) {
        var sql = "UPDATE ${table} SET badgenum = 0 WHERE chatid = '$chatId'"
        LogKit.p(sql)
        db.execSQL(sql)
    }

    fun delete(chatId: String) {
        var sql = "DELETE FROM ${table} WHERE chatid = '${chatId}'"
        db.execSQL(sql)
    }
}