package com.dqd2022.helpers

import com.alibaba.fastjson.JSONObject
import com.dqd2022.api.API
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.dto.StorageInsertUserMessageDto
import com.dqd2022.helpers.qiniu.QiniuUtils
import com.dqd2022.model.ChatlistModel
import com.dqd2022.model.RoomMessageTable
import com.dqd2022.model.UserMessageModel
import kit.FileKit
import kit.LogKit
import kit.StringKit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openapitools.client.models.T3imapiv1PushMidReq
import org.openapitools.client.models.T3imapiv1PushMidRes
import org.openapitools.client.models.T3imapiv1PushRoomReq
import org.openapitools.client.models.T3imapiv1PushRoomRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


class SendMessageReqDto {
    var LocalMsgId: String = ""

    @JvmField
    var Type = 0

    @JvmField
    var ToUsers = 0

    @JvmField
    var Message: String = ""

    @JvmField
    var RoomId: String? = ""
}


object ImSendMessageHelper {
    val userMessageTable = UserMessageModel()
    val roomMessageTable = RoomMessageTable()
    var isSendText = false

    @JvmStatic
    fun sendText(msgType: Int, message: String, toUser: Int, roomId: Int): String {
        val msg = JSONObject()
        msg.put("content", message)
        isSendText = true
        return sendMessage(msgType, msg, toUser, roomId)
    }

    // 资源上传、发送
    @JvmStatic
    fun sendMessage(msgType: Int, message: JSONObject, toUser: Int, roomId: Int): String {
        var msg: String;
        val localMsgId = StringKit.uuid()
        if (msgType == MessageType.Text) {
            msg = message.getString("content")
        } else if (msgType == MessageType.Photo || msgType == MessageType.Voice) {
            saveMessage(localMsgId, msgType, message.toJSONString(), toUser, roomId)
            GlobalScope.launch {
                val obsUrl = QiniuUtils.imChatUpload(message.getString("uri"), message.getString("mime"))
                message.put("uri", obsUrl)
                message.remove("mime")
                send(localMsgId, msgType, message.toString(), toUser, roomId)
            }
            return localMsgId
        } else if (msgType == MessageType.Video) {
            saveMessage(localMsgId, msgType, message.toJSONString(), toUser, roomId)
            GlobalScope.launch {
                val videoUrl = QiniuUtils.imChatUpload(message.getString("uri"), message.getString("video_mime"))
                val coverUrl = QiniuUtils.imChatUpload(message.getString("cover"), message.getString("cover_mime"))
                message.put("uri", videoUrl)
                message.put("cover", coverUrl)
                message.remove("video_mime")
                message.remove("cover_mime")
                send(localMsgId, msgType, message.toString(), toUser, roomId);
            }
            return localMsgId
        } else if (msgType == MessageType.VideoShare) {
            saveMessage(localMsgId, msgType, message.toJSONString(), toUser, roomId)
            GlobalScope.launch {
                var coverUri = message.getString("cover")
                var videoUri = message.getString("uri")
                if (!coverUri.startsWith("http")) {
                    val mime = "image/" + FileKit.getSuffix(coverUri)
                    coverUri = QiniuUtils.imChatUpload(coverUri, mime)
                }
                if (!videoUri.startsWith("http")) {
                    val mime = "video/" + FileKit.getSuffix(videoUri)
                    videoUri = QiniuUtils.imChatUpload(videoUri, mime)
                }
                message.put("cover", coverUri)
                message.put("uri", videoUri)
                send(localMsgId, msgType, message.toString(), toUser, roomId);
            }
            return localMsgId
        } else if (msgType == MessageType.ModifyRoomName) {
            // 本地的
            val content = message.getString("content")
            saveMessage(localMsgId, msgType, content, toUser, roomId)
            updateChatlist(msgType, content, toUser, roomId)
            // 发送的
            message.remove("content")
            send(localMsgId, msgType, message.toJSONString(), toUser, roomId)
            return localMsgId
        } else if (msgType == MessageType.RoomRemoveMember) {
            val msg = message.toJSONString()
            val content = message.getString("content")
            saveMessage(localMsgId, msgType, msg, toUser, roomId)
            send(localMsgId, msgType, message.toJSONString(), toUser, roomId)
            updateChatlist(msgType, content, toUser, roomId)
            return localMsgId
        } else if (isSendText) {
            msg = message.getString("content")
        } else {
            msg = message.toJSONString()
        }
        saveMessage(localMsgId, msgType, msg, toUser, roomId)
        send(localMsgId, msgType, msg, toUser, roomId)
        updateChatlist(msgType, msg, toUser, roomId)
        return localMsgId
    }

    private fun send(localMsgId: String, msgType: Int, message: String, toUser: Int, roomId: Int) {
        if (roomId == 0) {
            val req = SendPrivateMessageReqDto()
            req.Type = msgType
            req.ToUsers = toUser
            req.Message = message
            sendPrivateMessage(req, localMsgId)
        } else {
            val req = SendRoomMessageReqDto()
            req.Type = msgType
            req.ToUsers = toUser
            req.Message = message
            req.RoomId = roomId
            sendRoomMessage(req, localMsgId)
        }
    }

    private fun updateChatlist(msgType: Int, message: String, toUser: Int, roomId: Int) {
        var chatType: Int = 0
        var bizId: Int = 0
        if (roomId == 0) {
            chatType = ChatType.Private
            bizId = toUser
        } else {
            chatType = ChatType.Room
            bizId = roomId
        }
        val contactsId = ImHelpers.makeChatId(chatType, bizId.toString())
        val msgDesc = ImHelpers.getLastMsgDesc(msgType, message)
        ChatlistModel().updateAndStorageChatlist(contactsId = contactsId, msgTime = Date().time, msgDesc = msgDesc, onChatting = true)
    }

    // 消息在发送前应该先保存到本地，以便实现 ACK 机制重发消息
    @JvmStatic
    fun saveMessage(localMsgId: String, msgType: Int, message: String, toUser: Int, roomId: Int) {
        if (roomId > 0) {
            val data = RoomMessageTable.StorageInsertRoomMessageDto()
            data.localMsgId = localMsgId
            data.serverMsgId = ""
            data.roomId = roomId
            data.msgType = msgType
            data.fromUser = App.myUserId.toString()
            data.message = message
            roomMessageTable.insert(data)
        } else {
            val data = StorageInsertUserMessageDto()
            data.localMsgId = localMsgId
            data.message = message
            data.belong = App.myUserId
            data.msgType = msgType
            data.fromUser = App.myUserId
            data.toUser = toUser
            userMessageTable.insert(data)
        }
    }

    // 发私聊消息
    @JvmStatic
    fun sendPrivateMessage(dto: SendPrivateMessageReqDto, localMsgId: String? = ""): String {
        // 发送
        val req = T3imapiv1PushMidReq(dto.Type, dto.Message, dto.ToUsers.toString())
        API().Chat.imlogicPushMidPost(req).enqueue(object : Callback<T3imapiv1PushMidRes?> {
            override fun onResponse(call: Call<T3imapiv1PushMidRes?>, response: Response<T3imapiv1PushMidRes?>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (localMsgId != null)
                        userMessageTable.updateMessageTime(localMsgId, res!!.serverMsgId, res.time.toBigInteger().toLong())
                }
            }

            override fun onFailure(call: Call<T3imapiv1PushMidRes?>, t: Throwable) {
                LogKit.p("[私聊消息发送失败]", t.message)
                t.printStackTrace()
            }
        })
        return localMsgId ?: ""
    }

    // 发群聊消息
    @JvmStatic
    fun sendRoomMessage(dto: SendRoomMessageReqDto, localMsgId: String? = ""): String {
        // 发送
        val req = T3imapiv1PushRoomReq(dto.Type, dto.Message!!, dto.RoomId.toString(), dto.ToUsers.toString())
        API().Chat.imlogicPushRoomPost(req).enqueue(object : Callback<T3imapiv1PushRoomRes?> {
            override fun onResponse(call: Call<T3imapiv1PushRoomRes?>, response: Response<T3imapiv1PushRoomRes?>) {
                LogKit.p(response)
                if (!response.isSuccessful) return
                LogKit.p("dfdsfdsfdfds")
                val res = response.body()
                if (localMsgId != null)
                    roomMessageTable.updateMessageTime(localMsgId, res!!.serverMsgId, res.time.toBigInteger().toLong())
            }

            override fun onFailure(call: Call<T3imapiv1PushRoomRes?>, t: Throwable) {
                LogKit.p("[群消息发送失败]", t.message)
            }
        })
        return localMsgId ?: ""
    }

    @JvmStatic
    fun makeVideoMsgBody(cover: String?, width: Int, hight: Int, videoUrl: String?): JSONObject {
        val msgBody = JSONObject()
        msgBody["cover"] = cover
        msgBody["w"] = width
        msgBody["h"] = hight
        msgBody["uri"] = videoUrl
        // msgBody.put("video_mime", result.get(0).getMimeType()); // 之前这个 video_mime 是给 js 上传 azure 时用的
        return msgBody
    }


}


// 发私聊消息
open class SendPrivateMessageReqDto {
    @JvmField
    var Type = 0

    @JvmField
    var ToUsers = 0

    @JvmField
    var Message: String = ""
}

// 发群聊消息
class SendRoomMessageReqDto : SendPrivateMessageReqDto() {
    @JvmField
    var RoomId: Int = 0
}
