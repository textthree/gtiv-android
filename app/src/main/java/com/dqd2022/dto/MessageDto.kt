package com.dqd2022.dto

import kit.StringKit
import kit.TimeKit

class MessageDto {
    // 昵称
    @JvmField
    var nickname: String? = null

    // 头像
    @JvmField
    var avatar: String? = null
    var time: String? = null
        private set
    var timeLong: Long? = null
        private set

    // 消息内容
    @JvmField
    var content: String? = null

    // 消息类型
    @JvmField
    var msgType = 0

    // 用户 id
    @JvmField
    var fromUserId: String? = null

    // 判断这套消息是否用户自己发的
    @JvmField
    var isMine: Boolean? = null

    // 消息 id
    @JvmField
    var localMsgId: String? = null

    @JvmField
    var serverMsgId: String? = null
    var id: Long? = null

    @JvmField
    var senderQuited: Boolean = false

    // 时间
    fun setTime(language: String, time: String?) {
        val t = StringKit.parseLong(time)
        timeLong = t
        if (language == "zh") {
            this.time = TimeKit.Stamp2ago(t)
        } else {
            this.time = TimeKit.Stamp2agoEn(t)
        }
    }

    fun setTime(language: String, time: Long) {
        timeLong = time
        if (language == "zh") {
            this.time = TimeKit.Stamp2ago(time)
        } else {
            this.time = TimeKit.Stamp2agoEn(time)
        }
    }
}

class StorageInsertUserMessageDto {

    @JvmField
    var localMsgId = ""

    @JvmField
    var serverMsgId = ""

    var chatNo = ""

    @JvmField
    var belong = 0

    @JvmField
    var msgType = 0

    @JvmField
    var message = ""

    @JvmField
    var fromUser = 0

    @JvmField
    var toUser = 0
}
