package com.dqd2022.dto

class UserinfoRes {
    var UserId = ""
    var UserRole = 0
    var Nickname = ""
    var Username = ""
    var Avatar = ""
    var Gender = 0
    var Birthday = ""
    var LastLoginTime = 0
    var Version = 0
    var ContactsVersion = 0
    var FansNum = 0
    var FollowNum = 0
    var SupportNum = 0
    var CreateVideoNum = 0
    var CollectVideoNum = 0
}

class ContactsItem {
    var UserId = ""
    var Username = ""
    var Nickname = ""
    var Avatar = ""
    var Gender = 0
    var Deleted = 0
}

class ContactsListRes : CommonResDto() {
    var List = arrayOf<ContactsItem>();
}

class RoomListItem {
    var RoomId = ""
    var RoomName = ""
    var Avatar = ""
    var MemberNum = 0
}

class RoomListRes : CommonResDto() {
    var List = arrayOf<RoomListItem>();
}

class MyUserListRes : CommonResDto() {
    var List = arrayOf<MyUserListItem>();
}

class MyUserListItem {
    var Id = 0
    var Nickname = ""
    var Avatar = ""
    var CreateTime: Long = 0
}

