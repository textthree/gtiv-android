package com.dqd2022.model

import com.alibaba.fastjson.JSON
import com.dqd2022.constant.CachePath
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.dto.MessageDto
import com.dqd2022.helpers.App
import kit.HttpKit
import kit.LogKit
import java.util.Collections

class MessageModel {
    val db = App.getDb()

    // 查询消息
    fun getList(chatType: Int, bizId: Int, page: Int, rows: Int): List<MessageDto?> {
        var list: MutableList<MessageDto>
        if (chatType == ChatType.Private) {
            list = UserMessageModel().getList(bizId, page, rows)
        } else {
            list = RoomMessageTable().getList(bizId, page, rows)
        }
        // 查的时候是倒序，显示的时候是正序
        Collections.reverse(list)
        return list
    }


    // 渲染时开线程异步下载资源并更新数据库
    fun downloadAssets(chatType: Int, msgType: Int, msgId: String, url: String?, message: String?) {
        try {
            val T = Thread {
                if (url != null && url.startsWith("http")) {
                    var dir: String? = ""
                    when (msgType) {
                        MessageType.Photo -> dir = CachePath.chatPhotoDir(App.context)
                        MessageType.Voice -> dir = CachePath.chatVoiceDir(App.context)
                    }
                    val sql: String
                    val segments = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val localPath = HttpKit().downloadFileAsync(url, dir, segments[segments.size - 1])
                    val jsonObject = JSON.parseObject(message)
                    jsonObject["url"] = localPath
                    val msg = jsonObject.toString()
                    if (chatType == ChatType.Private) {
                        sql = "UPDATE usermessageentity SET message = '$msg' WHERE server_msgid = '$msgId'"
                        db.execSQL(sql)
                    } else {
                        sql = "UPDATE roommessageentity SET message = '$msg'"
                        db.execSQL(sql)
                    }
                    LogKit.p("下载资源", url, "更新记录", sql)
                }
            }
            T.start()
        } catch (e: Exception) {
            LogKit.p("下载资源出错")
            e.printStackTrace()
        }
    }


}