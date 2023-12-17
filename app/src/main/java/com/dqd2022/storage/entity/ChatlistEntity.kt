package com.dqd2022.storage.entity

import litepal.annotation.Column
import litepal.crud.LitePalSupport


class ChatlistEntity : LitePalSupport() {
    @Column(unique = true)
    private val chatId: String? = null

    @Column(index = true)
    private val belong = 0
    private val chatTitle: String? = null
    private val chatType = 0
    var badgeNum = 0
    private val lastMessageTime: Long? = null
    private val lastMessageDesc: String? = null
    private val topTime = 0
}