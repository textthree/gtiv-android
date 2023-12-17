package com.dqd2022.conn;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.CallAction;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.MessageType;
import com.dqd2022.dto.EvtBanned;
import com.dqd2022.dto.MessageDto;
import com.dqd2022.dto.RoomMemberInfoDto;
import com.dqd2022.dto.UserinfoDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.model.RoomMemberModel;
import com.dqd2022.page.im.chatting.ChattingActivity;
import com.dqd2022.page.webrtc.AnswerCallActivity;
import com.dqd2022.page.webrtc.CallActivity;
import com.dqd2022.model.ContactsModel;
import com.dqd2022.helpers.UserHelpers;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import kit.AppKit;
import kit.ArrayKit;
import kit.LogKit;

public class ReciveMessage {
    int fromUserId, chatType, msgType, roomId, bizId;
    Long msgTime;
    Boolean chatting = false, isMine = false;
    String serverMsgId, msgContent, contactsId, toUsers;
    HandleMessage handleMessage;

    ReciveMessage(String message, Long operation) {
        JSONObject msg = JSON.parseObject(message);
        serverMsgId = msg.getString("ServerMsgId");
        msgTime = msg.getLong("Time");
        fromUserId = msg.getInteger("FromUser");
        toUsers = msg.getString("ToUsers");
        if (fromUserId == App.myUserId) isMine = true;
        chatType = msg.getInteger("ChatType");
        msgType = msg.getInteger("MsgType");
        msgContent = msg.getString("Content");
        if (chatType == ChatType.Room) {
            roomId = msg.getInteger("RoomId");
            bizId = roomId;
        } else {
            bizId = fromUserId;
        }
        chatting = ImHelpers.chatting(chatType, bizId);
        contactsId = ImHelpers.makeChatId(chatType, String.valueOf(bizId));
        LogKit.p("[收到消息]", "operation:" + operation + " FromUserId:" + fromUserId +
                " chattingWith:" + ImHelpers.getChattingTarget(), " chatting:" + chatting, " contactsId:", contactsId);
        LogKit.p("[消息内容]", message);
        // 如果正在与聊天者对话，需要渲染消息列表
        // 大部分自己发给自己的消息不用渲染，发的时候本地渲染过了，个别消息本地还没渲染的通过服务端推送渲染，
        // 为什么要有个别消息，主要是懒、项目赶
        if (!isMine) {
            if (chatting) chattingRender();
            handleMessage = new HandleMessage(
                    chatType, chatting, roomId, contactsId, fromUserId, toUsers, msgType, serverMsgId, msgContent, msgTime);
            this.handleMessage();
        }
    }

    // 处理消息
    void handleMessage() {
        // 更新会话列表、存储消息
        switch (msgType) {
            // 打招呼消息
            case MessageType.SayHello:
                handleMessage.sayHello();
                break;
            // 文本消息
            case MessageType.Text:
                handleMessage.textMessage();
                break;
            // 图片消息
            case MessageType.Photo:
                handleMessage.pictureMessage();
                break;
            // 语音消息
            case MessageType.Voice:
                handleMessage.voiceMessage();
                break;
            // 视频消息
            case MessageType.Video:
                handleMessage.videoMessage();
                break;
            // 通过好友验证消息（对方同意了我的好友申请）
            case MessageType.NewFriend:
                handleMessage.newFriend();
                break;
            // 有人请求一对一通话
            case MessageType.ApplyOne2OneVideoCall:
                if (ChatType.Room == chatType) return;
                haveAcall(fromUserId);
                break;
            case MessageType.CallerHangUp:
                handleMessage.callerHangUp();
                break;
            case MessageType.ReceiverHangUp:
                handleMessage.receiverHangUp();
                break;
            // 新加入群
            case MessageType.NewRoom:
                LogKit.p("新加入群 " + msgContent + " ，重新建立连接。[TODO]: 后端直接处理，不需要客户端重连。");
                new ContactsModel().insertRoomById(msgContent, () -> {
                    ImHelpers.connClient.reconnect();
                });
                break;
            // 修改群名称
            case MessageType.ModifyRoomName:
                handleMessage.modifyRoomName();
                break;
            // 修改群头像
            case MessageType.ModifyRoomAvatar:
                handleMessage.modifyRoomAvata();
                break;
            // 群里踢人
            case MessageType.RoomRemoveMember:
                handleMessage.roomRemoveMember();
                break;
            // 对方把我删除
            case MessageType.DeleteContacts:
                new ContactsModel().updateField(contactsId, "isdeleted", "1");
                if (chatting) {
                    EventBus.getDefault().post(new EvtBanned(App.context.getString(R.string.unfriended)));
                }
                break;
            // 账号在其他地方登录
            case MessageType.OtherPlaceSignIn:
                App.context.sendBroadcast(new Intent(BroadCastKey.otherPlaceLogin.name()));
                ImHelpers.connClient.close();
                break;
            // 撤回消息
            case MessageType.Repeal:
                handleMessage.repealMsg();
                break;
            // 有人退群
            case MessageType.ExitGroup:
                handleMessage.exitGroup();
                break;
            // 禁言
            case MessageType.BanToPost:
                handleMessage.banned();
                break;
            // 解除禁言
            case MessageType.RelieveBan:
                handleMessage.relieveBan();
                break;
        }
    }

    // 正在聊天窗口中时，渲染聊天界面
    void chattingRender() {
        ChattingActivity chattingActivity = ChattingActivity.getInstance();
        // 以下消息类型不用渲染
        int[] dontRender = new int[]{
                MessageType.SayHello,
                MessageType.NewRoom,
                MessageType.DeleteContacts,
                MessageType.ApplyOne2OneVideoCall,
                MessageType.AcceptCall,
        };
        if (ArrayKit.inArray(msgType, dontRender)) return;
        // 撤回消息
        if (msgType == MessageType.Repeal) {
            Intent intent = new Intent();
            intent.setAction(BroadCastKey.chattingRepealMsg.name());
            intent.putExtra("serverMsgId", msgContent);
            chattingActivity.sendBroadcast(intent);
            // return;
        }
        // 更新聊天消息
        MessageDto item = new MessageDto();
        item.serverMsgId = serverMsgId;
        item.fromUserId = Integer.toString(fromUserId);
        item.content = msgContent;
        item.msgType = msgType;
        item.setTime(App.language, msgTime);
        item.isMine = false;
        if (chatType == ChatType.Private) {
            UserinfoDto userinfo = new UserHelpers().getFriendInfo(fromUserId);
            item.nickname = userinfo.nickname;
            item.avatar = userinfo.avatar;
        } else {
            RoomMemberInfoDto memberInfo = new RoomMemberModel().getMemberInfo(roomId, fromUserId);
            if (memberInfo == null) {
                memberInfo = new RoomMemberModel().cacheMember(roomId, fromUserId, null);
            }
            item.nickname = memberInfo.nickname;
            item.avatar = memberInfo.avatar;
        }
        chattingActivity.runOnUiThread(() -> ChattingActivity.getVm().renderReceiveMsg(item));
        // 禁言
        if (msgType == MessageType.BanToPost) {
            chattingActivity.runOnUiThread(() -> ChattingActivity.getVm().banned(msgContent));
        } else if (msgType == MessageType.RelieveBan) {
            chattingActivity.runOnUiThread(() -> ChattingActivity.getVm().relieveBan());
        }
    }

    // 有人请求一对一通话
    void haveAcall(int fromUserId) {
        // 正在通话中直接终止后续行为
        if (ImHelpers.isCalling()) {
            LogKit.p("正在忙线中...");
            return;
        }
        MainActivity mainActivity = MainActivity.getInstance();
        ImHelpers.setCalling(true);
        try {
            // 拉起来电界面
            Intent intent = new Intent(mainActivity, CallActivity.class);
            intent.putExtra("callAction", CallAction.ANSWER);
            intent.putExtra("callType", msgContent);
            intent.putExtra("bizId", fromUserId);
            mainActivity.startActivity(intent);

            // 锁屏唤醒
            if (AppKit.passwordLocked(mainActivity)) {
                Intent inte = new Intent(mainActivity, AnswerCallActivity.class);
                inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainActivity.startActivity(inte);
            }

            // 获取自己的进程 ID ，从任务栏把自己拉出来
            if (!AppKit.isRunningForeground(mainActivity)) {
                int taskId = mainActivity.getTaskId();
                AppKit.switchAppForeground(mainActivity, taskId);
                // 在锁屏或者刚切换到后台又立马去拉的情况下可能会拉取失败，所以延时重拉，拉不起来再启动
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    int count = 1;

                    @Override
                    public void run() {
                        if (!AppKit.isRunningForeground(mainActivity)) {
                            // 如果已经拉了两次还没拉起来，第三次使用启动 APP 的方式来唤醒 APP 到前台
                            if (count >= 3) {
                                LogKit.p("启动唤醒");
                                Intent i = mainActivity.getPackageManager().getLaunchIntentForPackage(mainActivity.getPackageName());
                                mainActivity.startActivity(i);
                                if (timer != null) timer.cancel();
                                return;
                            }
                            LogKit.p("taskId:" + taskId, "尝试第 " + count + " 次拉取");
                            AppKit.switchAppForeground(mainActivity, taskId);
                        } else {
                            LogKit.p("已处于前台");
                            if (timer != null) timer.cancel();
                        }
                        count++;
                    }
                }, 0, 3000);
            }
        } catch (Exception e) {
            LogKit.p("来电唤醒失败", e.getMessage());
            e.printStackTrace();
        } finally {
            ImHelpers.setCalling(false);
        }
    }


}
