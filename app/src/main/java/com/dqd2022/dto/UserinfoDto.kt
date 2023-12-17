package com.dqd2022.dto

class UserinfoDto {
    @JvmField
    var nickname = ""

    @JvmField
    var avatar = ""

    @JvmField
    var gender = 0

    @JvmField
    var username: String? = null
}

class RoomMemberInfoDto {
    var id = 0

    var userId = 0

    @JvmField
    var nickname = ""

    @JvmField
    var avatar = ""

    var createTime: Long = 0

    var role = 0

    // 是否刚从服务器拉下来插入到本地的记录
    var isNewInsert: Boolean? = false
}