package com.dqd2022.model

import android.content.Intent
import com.dqd2022.MainActivity
import com.dqd2022.api.ImbizApi
import com.dqd2022.constant.BroadCastKey
import com.dqd2022.constant.ChatType
import com.dqd2022.dto.ChatItemDto
import com.dqd2022.dto.ChatlistItemDto
import com.dqd2022.dto.DbChatlistItemDto
import com.dqd2022.dto.EvtUpdateChatlist
import com.dqd2022.dto.SyncPrivateMessageRes
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.SQLite
import com.dqd2022.helpers.UserHelpers
import com.dqd2022.storage.chatlistTable
import com.dqd2022.storage.entity.ChatlistEntity
import com.dqd2022.storage.entity.RoomMessageEntity

import kit.LogKit
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatlistModel {
    val repository = ChatlistEntity()

    // 去服务端同步消息，先同步完私聊消息再去同步群消息，涉及 UI 更新不做并发同步
    fun syncMessage(callback: (Int) -> Unit) {
        syncUserMessage(callback, { syncRoomMessage(callback) })
    }

    // 同步私聊消息
    fun syncUserMessage(
        callback: (Int) -> Unit,
        next: () -> Unit
    ) {
        val lastSyncTime = UserMessageModel().lastMsgTime
        ImbizApi.getInstance().syncPrivateMessage(lastSyncTime)
            .enqueue(object : Callback<SyncPrivateMessageRes> {
                override fun onResponse(
                    call: Call<SyncPrivateMessageRes>,
                    response: Response<SyncPrivateMessageRes>
                ) {
                    val res = response.body()
                    if (res?.List?.size ?: 0 > 0) {
                        val userMessageModel = UserMessageModel()
                        val chatlistTable = ChatlistTable()
                        val mp: MutableMap<String, ChatlistItemDto> = mutableMapOf()
                        res?.List?.forEach {
                            // 保存消息
                            val result = userMessageModel.insertOne(
                                it.MsgId,
                                it.MsgType,
                                it.Time,
                                it.Content,
                                it.FromUser
                            )
                            if (!result) return@forEach
                            // 会话列表
                            var contacts = UserHelpers().getFriendInfo(it.FromUser)
                            var chatItem = ChatlistItemDto()
                            chatItem.chatId =
                                ImHelpers.makeChatId(ChatType.Private, it.FromUser.toString())
                            chatItem.avatar = contacts.avatar
                            chatItem.title = contacts.nickname
                            var badgeNum = mp[it.FromUser.toString()]?.badgeNum ?: 0
                            chatItem.badgeNum = badgeNum + 1
                            chatItem.time = it.Time
                            chatItem.lastMsgDesc = ImHelpers.getLastMsgDesc(it.MsgType, it.Content)
                            mp[it.FromUser.toString()] = chatItem
                            for ((k, v) in mp) {
                                val item = DbChatlistItemDto()
                                item.chatId = v.chatId
                                item.chatType = ChatType.Private
                                item.title = v.title
                                item.avatar = v.avatar
                                item.lastMessageTime = v.time
                                item.lastMessageDesc = v.lastMsgDesc
                                item.badgeNum = v.badgeNum
                                chatlistTable.saveChatItem(item)
                            }
                        }
                        val count = res?.List?.size ?: 0
                        callback(count)
                        LogKit.p("成功同步私聊消息", count, "条。lastSyncTime:", lastSyncTime)
                    }
                    next()
                }

                override fun onFailure(call: Call<SyncPrivateMessageRes?>, t: Throwable) {
                    LogKit.p("[同步用户消息错误]", t.message)
                    t.printStackTrace()
                }
            })
    }

    // 去服务端同步群消息
    fun syncRoomMessage(callback: (Int) -> Unit) {
        Thread(Runnable {
            val contactTable = ContactsModel()
            var rooms = contactTable.getContactsList(ChatType.Room)
            LogKit.p("群列表为空，不进行同步")
            if (rooms == null) return@Runnable;
            rooms?.forEach {
                var response =
                    ImbizApi.getInstance().syncRoomMessage(it.bizId, it.lastSyncMsgTime).execute()
                var res = response.body()
                val list = res?.List
                var msgCount = res?.List?.size ?: 0
                //LogKit.p( "同步群 [" + it.nickname + " ID: " + it.contactsId + "] 的消息，", msgCount, "条", "lastSyncTime:" + it.lastSyncMsgTime )
                var syncTime: Long = 0
                if (msgCount > 0) {
                    if (list != null && list.size > 0) {
                        syncTime = list.get(0).Time
                    }
                    val roomId = it.bizId
                    var roomInfo = ContactsModel().getOne(ChatType.Room, roomId)
                    if (roomInfo == null) {
                        LogKit.p("同步群消息出错 ------------------------ roomId:", roomId)
                        return@forEach
                    }
                    var chatlistTable = ChatlistTable()
                    var reverseList = list?.reversed()!!
                    reverseList.forEach {
                        // 更新会话
                        var item = DbChatlistItemDto()
                        item.chatId =
                            ImHelpers.makeChatId(ChatType.Room, roomId.toString())
                        item.chatType = ChatType.Room
                        item.title = roomInfo.nickname
                        item.avatar = roomInfo.avatar
                        item.lastMessageTime = it.Time
                        item.lastMessageDesc = ImHelpers.getLastMsgDesc(it.MsgType, it.Content)
                        item.badgeNum = msgCount
                        chatlistTable.saveChatItem(item)
                        // 写入消息
                        val message = RoomMessageEntity()
                        message.roomId = roomId
                        message.server_msgid = it.MsgId
                        message.message = it.Content
                        message.server_time = it.Time
                        message.fromUser = it.FromUser
                        message.type = it.MsgType
                        message.save()
                    }
                    callback(msgCount)
                    // 更新群消息同步时间
                    if (syncTime > 0)
                        contactTable.updateRoomMsgSyncTime(roomInfo.contactsId, syncTime)
                }
            }
            LogKit.p(rooms.size.toString() + " 个群的消息同步完成")
        }).start()
    }

    fun countBadge(): Int {
        val sql = "SELECT sum(badgenum) sum FROM ${chatlistTable} WHERE belong = ${App.myUserId}"
        val cursor = App.getDb().rawQuery(sql, null)
        if (cursor.count > 0) {
            cursor.moveToNext()
            val count = SQLite.getIntFromCursor(cursor, "sum")
            return count
        }
        return 0
    }

    // 更新会话列表、不存在的会创建
    fun updateAndStorageChatlist(
        contactsId: String,
        msgTime: Long,
        msgDesc: String,
        onChatting: Boolean? = false,
        argAvatar: String? = "",
        argNick: String? = ""
    ) {
        var chattype = ImHelpers.getChatTypeByChatId(contactsId)
        val userHelpers = UserHelpers()
        var avatar = ""
        var nick = ""
        var isTempChat = false
        var contacts = ContactsModel().getById(contactsId)
        if (contacts != null) {
            avatar = contacts.avatar
            nick = contacts.nickname
        } else {
            if (chattype == ChatType.Private) {
                val fromUserId = ImHelpers.getBizIdByChatId(contactsId)
                val userinfo = userHelpers.getUserinfoFromServer(fromUserId)
                avatar = userinfo.avatar
                nick = userinfo.nickname
            }
            isTempChat = true
        }
        // 修改群头像、名称时会传过来修改后的头像或名称
        if (argNick != "") nick = argNick!!
        if (argAvatar != "") avatar = argAvatar!!

        var item = ChatItemDto()
        item.contactsId = contactsId
        item.avatar = avatar
        item.name = nick
        item.badgeNum = 1
        item.chatType = chattype;
        item.lastMessageTime = msgTime
        item.lastMessageDesc = msgDesc
        // 更新
        if (onChatting == null || !onChatting) {
            ImHelpers.totalBadgeIncrease(1)
            MainActivity.getInstance().sendBroadcast(Intent(BroadCastKey.refreshBadge.name))
        } else {
            item.badgeNum = 0
        }
        EventBus.getDefault().post(EvtUpdateChatlist(contactsId, item))
        // 存储会话
        val data = DbChatlistItemDto()
        data.chatId = contactsId
        data.chatType = chattype
        data.title = if (isTempChat) "$nick (Temporary Session)" else nick
        data.avatar = avatar
        data.lastMessageTime = msgTime
        data.lastMessageDesc = msgDesc
        ChatlistTable().saveChatItem(data)
    }


}