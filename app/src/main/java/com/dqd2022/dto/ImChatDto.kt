package com.dqd2022.dto

class ChatItemDto {
    var id: Long? = 0
    var contactsId = ""
    var name = ""
    var avatar = ""
    var chatType = 0
    var topTime = 0
    var badgeNum = 0
    var lastMessageDesc = ""
    var lastMessageTime: Long = 0
}

// 请求加我为好友的人
class AddMeListItem {
    var UserId = 0
    var Avatar = ""
    var Nick = ""
    var Msg = ""
    var Time: Long = 0
    var Gender = 0
    var Username = ""
}

class AddMeListRes : CommonResDto() {
    var List = arrayOf<AddMeListItem>();
}


class UserMessageListItem {
    var MsgId = ""
    var MsgType = 0
    var FromUser = 0
    var Content = ""
    var Time: Long = 0
}

class SyncPrivateMessageRes {
    var List = arrayOf<UserMessageListItem>();
}

// 同步群聊消息
class SyncRoomMessageReq {
    var RoomId = 0
    var LastMessageTime = 0
}

class RoomMsgListItem {
    var MsgId = ""
    var MsgType = 0
    var FromUser = 0
    var Content = ""
    var Time: Long = 0
}

class SyncRoomMessageRes {
    var List = arrayOf<RoomMsgListItem>();
}

// 会话列表
class ChatlistItemDto {
    var chatId = ""
    var title = ""
    var avatar = ""
    var lastMsgDesc = ""
    var time: Long = 0
    var badgeNum = 0
}

// 会话列表存储 dto
class DbChatlistItemDto {
    var chatId = ""; // 与联系人 id 一致
    var title = "";
    var chatType = 0;
    var avatar = "";
    var lastMessageTime: Long = 0;
    var lastMessageDesc = "";
    var badgeNum: Int? = 0;
}