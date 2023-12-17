package com.dqd2022.model

import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.constant.RoomMemberRole
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import kit.LogKit
import kit.TimeKit
import org.openapitools.client.models.T3imapiv1InviteMemberReq
import org.openapitools.client.models.T3imapiv1InviteMemberRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomModel {


    // 创建群
    fun createRoom(userIds: MutableList<Int>) {
        API().Room.roomInvitePost(T3imapiv1InviteMemberReq(userIds = userIds.joinToString(","))).enqueue(object : Callback<T3imapiv1InviteMemberRes> {
            override fun onResponse(call: Call<T3imapiv1InviteMemberRes>, response: Response<T3imapiv1InviteMemberRes>) {
                if (!response.isSuccessful) {
                    AlertUtils.toast("network error")
                    return
                }
                var res = response.body()!!
                if (res.apiCode > 0) {
                    AlertUtils.toast(res.apiMessage)
                    return
                }
                AlertUtils.toast(App.context.getString(R.string.sent))
                // 本地建群
                val contactsModel = ContactsModel()
                for (uid in userIds) {
                    val contacts = contactsModel.getOne(ChatType.Private, uid)
                    RoomMemberModel().insertOne(
                        res.roomId.toInt(),
                        uid,
                        contacts?.nickname ?: "",
                        contacts?.avatar ?: "",
                        "",
                        TimeKit.nowSecond(),
                        RoomMemberRole.member
                    )
                }
                contactsModel.insertRoomById(res.roomId)
                // 创建会话
                val chatId = ImHelpers.makeChatId(ChatType.Room, res.roomId)
                ChatlistModel().updateAndStorageChatlist(chatId, TimeKit.nowMillis(), res.message, argNick = res.roomName)

            }

            override fun onFailure(call: Call<T3imapiv1InviteMemberRes>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     *  旧群邀请人
     *  message 在邀请群成员时可以有值，即发送一条谁邀请谁到群里的消息
     */
    fun inviteMember(userIds: String, roomId: Int, message: String) {
        API().Room.roomInvitePost(
            T3imapiv1InviteMemberReq(userIds = userIds, roomId = roomId.toString(), message = message)
        ).enqueue(object : Callback<T3imapiv1InviteMemberRes> {
            override fun onResponse(call: Call<T3imapiv1InviteMemberRes>, response: Response<T3imapiv1InviteMemberRes>) {
                if (!response.isSuccessful) {
                    AlertUtils.toast("network error")
                    return
                }
                var res = response.body()!!
                if (res.apiCode > 0) {
                    AlertUtils.toast(res.apiMessage)
                    return
                }
                AlertUtils.toast(App.context.getString(R.string.sent))
                // 本地拉取成员
                Thread {
                    val list = userIds.split(",")
                    list.forEach {
                        LogKit.p("邀请：", it.toInt())
                        RoomMemberModel().cacheMember(roomId = roomId, userId = it.toInt())
                    }
                }.start()
            }

            override fun onFailure(call: Call<T3imapiv1InviteMemberRes>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}