package com.dqd2022.dto


// 清空聊天界面中的记录
data class EvtClearChatRecords(val msg: Boolean)

// 删除会话
data class EvtDeleteChatItem(val chatId: String)

// 禁言
data class EvtBanned(val msg: String)

// 更新会话列表
data class EvtUpdateChatlist(
    val contactsId: String,
    val item: ChatItemDto? = null,
    val nickname: String? = "",
    val avatar: String? = ""
)

// 刷新群人数
data class EvtUpdateRoomMemberNumber(val roomId: Int)

// 修改了群名称
data class EvtRoomNameChanged(val name: String)