package com.dqd2022.conn

import android.content.Intent
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.dqd2022.R
import com.dqd2022.constant.BroadCastKey
import com.dqd2022.constant.ChatType
import com.dqd2022.dto.AddMeListItem
import com.dqd2022.dto.EvtBanned
import com.dqd2022.dto.EvtUpdateChatlist
import com.dqd2022.helpers.App
import com.dqd2022.helpers.CacheHelpers
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.UserHelpers
import com.dqd2022.model.ChatlistModel
import com.dqd2022.model.ContactsModel
import com.dqd2022.model.RoomMemberModel
import com.dqd2022.model.RoomMessageTable
import com.dqd2022.model.UserMessageModel
import com.dqd2022.page.im.chatlist.ChatlistFragment
import kit.LogKit
import kit.VibrateKit
import org.greenrobot.eventbus.EventBus


class HandleMessage(
    val chatType: Int,
    val chatting: Boolean,
    val roomId: Int = 0,
    val chatId: String,
    val fromUserId: Int,
    val toUsers: String,
    val msgType: Int,
    val serverMsgId: String,
    val msgContent: String,
    val msgTime: Long
) {

    val userMessageModel = UserMessageModel()
    val roomMessageTable = RoomMessageTable()
    val chatlistModel = ChatlistModel()

    // chatlist 界面操作
    private fun runOnUI(fn: () -> Unit) {
        if (ChatlistFragment.getBinding() != null) {
            ChatlistFragment.getCtx().runOnUiThread {
                fn()
            }
        }
    }


    // 存储消息
    fun insertMessages() {
        if (chatType == ChatType.Private) {
            userMessageModel.insertOne(serverMsgId, msgType, msgTime, msgContent, fromUserId)
        } else {
            roomMessageTable.insertOne(roomId, fromUserId, serverMsgId, msgType, msgContent)
        }
    }


    // 发送通知栏消息、app 桌面图标上的角标
    fun updateImService() {
        try {
            ImHelpers.setImService(ImHelpers.getTotalBadge())
        } catch (e: Exception) {
            LogKit.p("发送通知栏消息失败：" + e.message)
        }
    }


    // 文本消息
    fun textMessage() {
        val msgDesc = ImHelpers.getLastMsgDesc(msgType, msgContent)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
        VibrateKit.messageVibrate(App.context)
        updateImService()
    }

    // 媒体消息
    fun mediaMessage() {
        val msgDesc = ImHelpers.getLastMsgDesc(msgType)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
        VibrateKit.messageVibrate(App.context)
        updateImService()
    }

    // 图片消息
    fun pictureMessage() {
        mediaMessage()
    }

    // 语音消息
    fun voiceMessage() {
        mediaMessage()
    }

    // 视频消息
    fun videoMessage() {
        mediaMessage()
    }

    // 打招呼消息
    fun sayHello() {
        ImHelpers.totalBadgeIncrease(1)
        val item = AddMeListItem()
        item.UserId = fromUserId
        val info = UserHelpers().getUserinfoFromServer(fromUserId)
        item.Avatar = info.avatar
        item.Nick = info.nickname
        item.Msg = msgContent
        item.Time = msgTime
        ImHelpers.insertAddMeList(item)
        runOnUI {
            ChatlistFragment.getBinding().addMeBadge.badgeNum.text =
                ImHelpers.getAddmeList().size.toString()
            App.context.sendBroadcast(Intent(BroadCastKey.refreshBadge.name))
        }
        updateImService()
    }

    // 通过好友验证 (对方同意加我为好友)
    fun newFriend() {
        val msgDesc = ImHelpers.getLastMsgDesc(msgType, msgContent)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        UserHelpers().saveContactsByUserId(fromUserId)
        insertMessages()
        VibrateKit.messageVibrate(App.context)
        updateImService()
    }

    // 呼叫方挂断
    fun callerHangUp() {
        val intent = Intent()
        intent.action = BroadCastKey.closeCallActivity.name
        App.context.sendBroadcast(intent)
        val msgDesc = ImHelpers.getLastMsgDesc(msgType, msgContent)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
    }

    // 接听方挂断
    fun receiverHangUp() {
        val intent = Intent()
        intent.action = BroadCastKey.closeCallActivity.name
        App.context.sendBroadcast(intent)
        val msgDesc = ImHelpers.getLastMsgDesc(msgType, msgContent)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
    }

    // 修改群名称
    fun modifyRoomName() {
        val msg = JSON.parseObject(msgContent)
        val operator = msg.getString("operator")
        val roomName = msg.getString("name")
        val desc = App.context.getString(R.string.modifyRoomName).replace("?", operator)
            .replace("_", roomName)
        val msgDesc = ImHelpers.getLastMsgDesc(desc)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, argNick = roomName, onChatting = chatting)
        // 改数据库
        ContactsModel().updateField(chatId, "nickname", roomName)
    }


    // 修改群头像
    fun modifyRoomAvata() {
        val avatar = msgContent
        EventBus.getDefault().post(EvtUpdateChatlist(chatId, item = null, avatar = avatar))
        val ret = CacheHelpers().downloadAvatar(avatar, true)
        ContactsModel().updateField(chatId, "avatar", ret.localFileUri)
    }


    // 撤回消息
    fun repealMsg() {
        val msgDesc = ImHelpers.getLastMsgDesc(msgType)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
    }

    // 群里 T 人
    fun roomRemoveMember() {
        val data = JSONObject.parseObject(msgContent)
        val content = data.getString("content")
        val msgDesc = ImHelpers.getLastMsgDesc(content)
        chatlistModel.updateAndStorageChatlist(chatId, msgTime, msgDesc, chatting)
        insertMessages()
        val userId = data.getString("userId")
        if (userId != "") RoomMemberModel().delete(roomId, userId.toInt())
        // 如果 T 的是自己
        if (chatting) {
            EventBus.getDefault().post(EvtBanned(App.context.getString(R.string.youBeenRemoved)))
            ImHelpers.connClient.reconnect()
        }
    }

    // 有人退群
    fun exitGroup() {
        RoomMemberModel().somebodyExitGroup(roomId, fromUserId)
    }

    // 禁言
    fun banned() {
        // 全体
        if (toUsers == "") {
            ContactsModel().updateField(chatId, "state", "2")
        }
    }

    // 解除禁言
    fun relieveBan() {
        // 全体
        if (toUsers == "") {
            ContactsModel().updateField(chatId, "state", "1")
        }
    }
}

