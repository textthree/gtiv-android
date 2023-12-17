package com.dqd2022.model

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.dqd2022.api.API
import com.dqd2022.dto.EvtUpdateRoomMemberNumber
import com.dqd2022.dto.RoomMemberInfoDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.CacheHelpers
import com.dqd2022.helpers.SQLite
import com.dqd2022.storage.roomMemberTable
import com.dqd2022.storage.roomMessageTable
import kit.LogKit
import org.greenrobot.eventbus.EventBus
import java.io.IOException

class RoomMemberModel {
    var table = roomMemberTable
    private val db: SQLiteDatabase

    init {
        db = App.getDb()
    }

    fun getMemberInfo(roomId: Int, userId: Int): RoomMemberInfoDto? {
        val sql = "SELECT nickname, avatar, role FROM $table WHERE room_id = $roomId AND user_id = $userId"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count == 0) {
            return null
        }
        cursor.moveToNext()
        val avatarIndex = cursor.getColumnIndex("avatar")
        val nickIndex = cursor.getColumnIndex("nickname")
        var ret = RoomMemberInfoDto()
        ret.avatar = cursor.getString(avatarIndex)
        ret.nickname = cursor.getString(nickIndex)
        ret.role = SQLite.getIntFromCursor(cursor, "role")
        return ret
    }

    // 获取群成员信息
    private fun getRoomMemberInfoFromServer(roomId: Int, userId: Int): RoomMemberInfoDto? {
        try {
            val res = API().Room.roomMemberInfoGet(roomId.toString(), userId.toString()).execute()
            if (res.isSuccessful) {
                val body = res.body()
                val ret = RoomMemberInfoDto()
                ret.avatar = body!!.avatar
                ret.nickname = body.nickname
                ret.createTime = body.createTime.toLong()
                ret.role = body.role
                return ret
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return null
    }

    // inviteMmeber 为 true 代表邀请新成员，不存在的记录更新，已存在的记录 update
    fun cacheMember(roomId: Int, userId: Int, item: RoomMemberInfoDto? = null): RoomMemberInfoDto? {
        var ret = RoomMemberInfoDto()
        var localAvatar = ""
        if (item != null) {
            if (item.avatar != "") {
                val cache = CacheHelpers()
                val ret = cache.downloadAvatar(App.context, item.avatar, true)
                localAvatar = ret.localFileUri
            }
            ret.nickname = item.nickname
            ret.avatar = item.avatar
            ret.createTime = item.createTime
            ret.role = item.role
        } else {
            val info = getRoomMemberInfoFromServer(roomId, userId) ?: return null
            if (info.avatar != "") {
                val cache = CacheHelpers()
                val ret = cache.downloadAvatar(App.context, info?.avatar, true)
                localAvatar = ret.localFileUri
            }
            ret.nickname = info.nickname
            ret.avatar = info.avatar
            ret.createTime = info.createTime
            ret.role = info.role
        }
        insertOne(roomId, userId, ret.nickname, localAvatar, ret.avatar, ret.createTime, ret.role)
        return ret
    }

    fun insertOne(roomId: Int, userId: Int, nick: String, localAvatar: String, avatar: String, createTime: Long, role: Int) {
        try {
            val sql = "INSERT INTO " + table + "(room_id, user_id, nickname, avatar, avatar_origin, create_time, role) " +
                    "VALUES( $roomId , $userId, '${nick}', '${localAvatar}', '${avatar}', ${createTime}, ${role})"
            db.execSQL(sql)
        } catch (e: SQLiteConstraintException) {
            LogKit.p("[插入群成员失败]", e.message)
        }
    }


    fun countRoomMember(roomId: Int): Int {
        val sql = "SELECT COUNT(*) count FROM $table WHERE room_id = $roomId"
        val cursor = db.rawQuery(sql, null)
        if (cursor.count == 0) return 0;
        cursor.moveToNext()
        return cursor.getInt(0)
    }

    fun localMemberList(roomId: Int, page: Int, rows: Int): Array<RoomMemberInfoDto>? {
        //if (page <= 2) return mockData(1, 50) else return mockData(1, 3)
        val skip = (page - 1) * rows
        val sql = "SELECT id, user_id, avatar, nickname, role FROM $table " +
                " WHERE room_id = $roomId ORDER by create_time ASC LIMIT $skip, $rows"
        LogKit.p(sql)
        val rows = db.rawQuery(sql, null)
        val ret = Array(rows.count) { RoomMemberInfoDto() }
        var i = 0
        while (rows.moveToNext()) {
            val row = SQLite(rows)
            ret[i].id = row.getInt("id")
            ret[i].userId = row.getInt("user_id")
            ret[i].role = row.getInt("role")
            ret[i].avatar = row.getString("avatar")
            ret[i].nickname = row.getString("nickname")
            i++
        }
        LogKit.p(ret, ret.size)
        return ret
    }

    fun remoteMemberList(roomId: Int, page: Int, rows: Int): Array<RoomMemberInfoDto>? {
        //LogKit.p("去服务器看看有没有新成员可以拉取")
        //return mockData(2, 3)
        val res = API().Room.roomMemberListGet(roomId = roomId.toString(), page = page, rows = rows).execute()
        if (!res.isSuccessful) return null
        val rs = res.body()
        if (rs?.apiCode != 0 || rs.list == null) return null
        var ret = Array(rs.list?.size!!) { RoomMemberInfoDto() }
        rs.list.forEachIndexed() { index, it ->
            ret[index].userId = it.userId.toInt()
            ret[index].nickname = it.nickname
            ret[index].avatar = it.avatar
            ret[index].role = it.role.toInt()
            //ret[index].createTime = it.createTime?.toLong()!!
            ret[index].createTime = 0
            // 存储
            val item = RoomMemberInfoDto()
            item.userId = ret[index].userId
            item.avatar = ret[index].avatar
            item.nickname = ret[index].nickname
            item.role = ret[index].role
            item.createTime = ret[index].createTime
            val result = cacheMember(roomId, ret[index].userId, item)
            if (result != null) ret[index].isNewInsert = true
        }
        return ret
    }

    private fun mockData(type: Int, quntity: Int): Array<RoomMemberInfoDto> {
        val rt = Array(quntity) { RoomMemberInfoDto() }
        for (i in 0..quntity - 1) {
            val item = RoomMemberInfoDto()
            if (type == 1) {
                item.nickname = "本地数据"
                item.avatar = "file:///data/user/0/com.dqd2022/files/t3im/storage/f9201b225607ccdd7a54b59d95a2e2f1"
            } else {
                item.avatar = "XXXX"
                item.nickname = "远程数据"
                item.isNewInsert = true
            }
            rt[i] = item
        }
        return rt
    }

    // 获取所有群成员的 userid
    fun getAllMemberIds(roomId: Int): ArrayList<Int> {
        val ret = ArrayList<Int>()
        val sql = "SELECT user_id FROM $table WHERE room_id = $roomId"
        val rows = db.rawQuery(sql, null)
        if (rows.count == 0) {
            return ret
        }
        while (rows.moveToNext()) {
            ret.add(SQLite.getIntFromCursor(rows, "user_id"))
        }
        return ret
    }

    // 他人退群
    fun somebodyExitGroup(roomId: Int, fromUserId: Int) {
        try {
            var sql = "DELETE FROM $roomMemberTable WHERE room_id = $roomId AND user_id = $fromUserId"
            db.execSQL(sql)
            sql = "UPDATE $roomMessageTable SET sender_quited = 1 WHERE roomid = $roomId AND fromuser = $fromUserId"
            db.execSQL(sql)
        } catch (e: SQLiteException) {
            LogKit.p("[他人退群 SQL 执行失败]", e.message)
        }
        EventBus.getDefault().post(EvtUpdateRoomMemberNumber(roomId))
    }

    // 删除成员
    fun delete(roomId: Int, userId: Int) {
        var sql = "DELETE FROM $table WHERE room_id = $roomId AND user_id = $userId"
        db.execSQL(sql)
        sql = "UPDATE $roomMessageTable SET sender_quited = 1 WHERE roomid = $roomId AND fromuser = $userId"
        db.execSQL(sql)
    }

}