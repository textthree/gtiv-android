package com.dqd2022.helpers;

import android.app.Activity;
import android.content.Intent;

import com.dqd2022.R;
import com.dqd2022.api.ImbizApi;
import com.dqd2022.conn.ConnClient;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.constant.MessageType;
import com.dqd2022.dto.AddMeListItem;
import com.dqd2022.dto.AddMeListRes;
import com.dqd2022.model.ChatlistModel;
import com.dqd2022.page.im.chatting.ChattingActivity;
import com.dqd2022.services.ImService;
import com.dqd2022.model.ContactsModel;

import java.util.ArrayList;

import kit.LogKit;
import kit.VibrateKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImHelpers {
    private static String chattingTarget;     // 正在与谁聊天
    public static volatile boolean calling; // 是否正在进行 webrtc 通话
    private static ArrayList<AddMeListItem> addMeList = new ArrayList<>();
    public static volatile ConnClient connClient;


    public static void goChatting(Activity activity, String chatId, String nick) {
        int chatType, bizId = 0;
        String[] seg = chatId.split("_");
        bizId = Integer.valueOf(seg[1]);
        if (seg[0].equals("user")) {
            chatType = ChatType.Private;
        } else {
            chatType = ChatType.Room;
        }
        goChatting(activity, chatType, bizId, nick);
    }


    public static void goChatting(Activity activity, int chatType, int bizId, String nick) {
        setChattingTarget(chatType, bizId);
        Intent intent = new Intent(activity.getApplicationContext(), ChattingActivity.class);
        intent.putExtra("bizId", bizId);
        intent.putExtra("chatType", chatType);
        intent.putExtra("nickname", nick);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }

    // 标识当前正在与谁聊天，chatType + bizId 如：user223、room165
    private static void setChattingTarget(int chatType, int bizId) {
        chattingTarget = makeChattingWithWho(chatType, bizId);
    }

    public static String getChattingTarget() {
        return chattingTarget;
    }

    // 退出聊天窗口时清除聊天目标标识
    public static void clearChattingTarget() {
        chattingTarget = null;
    }

    // 收到信息消息时，判断是否正在与来信者聊天
    public static boolean chatting(int chatType, int bizId) {
        if (chattingTarget == null) return false;
        if (chatType == ChatType.Private && makeChattingWithWho(chatType, bizId).equals(chattingTarget)) {
            return true;
        } else if (chatType == ChatType.Room && makeChattingWithWho(chatType, bizId).equals(chattingTarget)) {
            return true;
        }
        return false;
    }

    // 组装聊天对象标识
    private static String makeChattingWithWho(int chatType, int bizId) {
        if (chatType == 1) {
            return "user" + bizId;
        } else {
            return "room" + bizId;
        }
    }

    public static void setCalling(boolean calling) {
        ImHelpers.calling = calling;
    }

    public static boolean isCalling() {
        return calling;
    }


    // 从缓存中加载信息到内存
    public static void locaCache() {

    }

    /**
     * 组装联系人表/会话列表的主键id，这两张表的 id 规则是一致的
     *
     * @param chatType
     * @param bizId    用户 id 或者房间 id
     * @return
     */
    public static String makeChatId(int chatType, String bizId) {
        if (chatType == ChatType.Private) {
            return "user_" + bizId + "_" + App.myUserId;
        } else {
            return "room_" + bizId + "_" + App.myUserId;
        }
    }

    public static int getBizIdByChatId(String chatId) {
        return Integer.valueOf(chatId.split("_")[1]);
    }

    public static int getChatTypeByChatId(String chatId) {
        String str = chatId.split("_")[0];
        if (str.equals("user")) {
            return ChatType.Private;
        } else {
            return ChatType.Room;
        }
    }

    // 根据消息类型转换 i18n 消息简述
    public static String getLastMsgDesc(int msgType, String text) {
        switch (msgType) {
            case MessageType.Photo:
                return App.context.getString(R.string.picture_message);
            case MessageType.Voice:
                return App.context.getString(R.string.voice_msg);
            case MessageType.Video:
                return App.context.getString(R.string.videoMsg);
            case MessageType.VideoShare:
                return App.context.getString(R.string.videoMsg);
            case MessageType.Repeal:
                return App.context.getString(R.string.repealMsg);
            case MessageType.BanToPost:
                return App.context.getString(R.string.bannedToPost);
            case MessageType.RelieveBan:
                return App.context.getString(R.string.relieveBannedUser);
            case MessageType.NewFriend:
                return App.context.getString(R.string.friendVerificationPassed);
            case MessageType.ApplyOne2OneVideoCall:
                return App.context.getString(R.string.videoIncomingCall);
            case MessageType.CallerHangUp:
                return App.context.getString(R.string.callNotConnected);
            case MessageType.ReceiverHangUp:
                return App.context.getString(R.string.rejectCall);
            default:
                if (text.length() > 20) {
                    return text.substring(0, 20) + "...";
                }
                return text;
        }
    }

    public static String getLastMsgDesc(String text) {
        if (text.length() > 20) {
            return text.substring(0, 20) + "...";
        }
        return text;
    }

    public static String getLastMsgDesc(int msgType) {
        return getLastMsgDesc(msgType, "");
    }


    // 角标总数 start
    public static void totalBadgeIncrease(int num) {
        App.mmkv.putInt(MMKVkey.imTotalBadge.name(), getTotalBadge() + num);
    }

    public static void totalBadgeReduce(int num) {
        App.mmkv.putInt(MMKVkey.imTotalBadge.name(), getTotalBadge() - num);
    }

    public static int getTotalBadge() {
        int total = App.mmkv.getInt(MMKVkey.imTotalBadge.name(), 0);
        if (total == 0) {
            total = new ChatlistModel().countBadge();
            total += addMeList.size();
            App.mmkv.putInt(MMKVkey.imTotalBadge.name(), total);
        }
        return total;
    }

    // 一键清除所有未读消息角标
    public static void clearAllChatBadgeNum() {
        if (addMeList.size() > 0)
            App.mmkv.putInt(MMKVkey.imTotalBadge.name(), addMeList.size());
        else {
            App.mmkv.remove(MMKVkey.imTotalBadge.name());
        }
    }


    public synchronized static void insertAddMeList(AddMeListItem item) {
        if (addMeList == null) {
            addMeList = new ArrayList<>();
        }
        addMeList.add(item);
    }

    public synchronized static void addMeListRemoveOne(int position) {
        addMeList.remove(position);
    }

    public static ArrayList<AddMeListItem> getAddmeList() {
        return addMeList;
    }


    // 启动连接，未登录就不用启动了
    public static void setImService(int badgeNum) {
        if (!App.isLogin()) return;
        try {
            Intent intent = new Intent(App.context, ImService.class);
            String title = App.context.getString(R.string.chatNotificationTitle);
            String content = App.context.getString(R.string.chatNotificationContent).replace("?", String.valueOf(badgeNum));
            intent.putExtra("badgeNum", badgeNum);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            App.context.startService(intent);
        } catch (Exception e) {
            LogKit.p("启动 ImService 失败，降级启动 ConnClient.start()。错误信息：" + e.getMessage());
        }
    }

    // 去服务端同步信息
    static void syncContactsAndMessage(int freshContactsVersion) {
        // 更新联系人，contactsNum 代表卸载重装，联系人列表空的的
        // reBuild 时其他线程查 contacts 表会查不到数据
        int contactsNum = new ContactsModel().getContactsNum();
        if ((freshContactsVersion > App.myContactsVersion) || contactsNum == 0) {
            LogKit.p("最新版本", freshContactsVersion, " > 本地版本", App.myContactsVersion, " -> 同步联系人");
            new ContactsModel().syncContacts(() -> {
                getMessageFromServer();
            }); // 这里面开了两个线程请求网络
        } else {
            getMessageFromServer();
        }

    }

    // 拉取新消息
    public static void getMessageFromServer() {
        new ChatlistModel().syncMessage(count -> {
            if (count > 0) {
                ImHelpers.totalBadgeIncrease(count);
                App.context.sendBroadcast(new Intent(BroadCastKey.refreshBadge.name()));
                // 通知 chatlist 更新 ui
                App.context.sendBroadcast(new Intent(BroadCastKey.syncMessageFinish.name()));
                // 拉到了新消息震动一下
                VibrateKit.messageVibrate(App.context);
            }
            return null;
        });
    }


    static void checkAddMeList() {
        ImbizApi.getInstance().getAddMeList().enqueue(new Callback<AddMeListRes>() {
            @Override
            public void onResponse(Call<AddMeListRes> call, Response<AddMeListRes> response) {
                if (!response.isSuccessful()) return;
                AddMeListRes res = response.body();
                if (res != null && res.getList() != null) {
                    for (AddMeListItem item : res.getList()) {
                        insertAddMeList(item);
                    }
                    // 通知 chatlist 更新 ui
                    App.context.sendBroadcast(new Intent(BroadCastKey.checkAddMeFinish.name()));
                }
            }

            @Override
            public void onFailure(Call<AddMeListRes> call, Throwable t) {
            }
        });
    }

}
