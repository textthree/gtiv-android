package com.dqd2022.constant;

public class MessageType {
    public static final int Text = 0;
    public static final int Photo = 1;
    public static final int Voice = 2;
    public static final int Video = 3;
    public static final int BanToPost = 4;   // 禁言，toUser 为 0 代表全体禁言
    public static final int RelieveBan = 5;  // 解除禁言
    public static final int Repeal = 6;      // 撤回消息
    public static final int SayHello = 7;    // 打招呼消息，通过好友验证直接发 text 消息
    public static final int NewFriend = 8;    // 通过好友验证
    public static final int NewRoom = 9;    // 被拉进新群
    public static final int ApplyOne2OneVideoCall = 10;  // 请求一对一通话，视频还是音频通话是在 messageBody 中
    public static final int CallerHangUp = 11; // 呼叫方挂断
    public static final int ReceiverHangUp = 12; // 被叫端挂断
    public static final int DeleteContacts = 13;    // 删除好友
    public static final int ModifyRoomName = 14;    // 修改群名称
    public static final int RoomRemoveMember = 15;    // 群里踢人
    public static final int OtherPlaceSignIn = 16;    // 其他地方登录
    public static final int AcceptCall = 17;    // 对方接听了我的通话请求
    public static final int VideoShare = 18;    // 视频分享给好友/群
    public static final int ExitGroup = 19;    // 有人退群
    public static final int InviteMember = 20;    // 拉人进群提示消息
    public static final int ModifyRoomAvatar = 21;    // 修改群头像


}

