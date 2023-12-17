package com.dqd2022.page.webrtc;

import android.app.Activity;
import android.content.Intent;

import androidx.lifecycle.ViewModel;

import com.dqd2022.R;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.CallAction;
import com.dqd2022.constant.CallType;
import com.dqd2022.constant.MessageType;
import com.dqd2022.databinding.WebrtcCallActivityBinding;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.helpers.ImSendMessageHelper;

import kit.LogKit;

public class CallViewModel extends ViewModel {
    WebrtcCallActivityBinding binding;
    public String nickname;
    public String avatar;
    public int callType, callAction;
    int bizId;


    public void setNickname(Activity activity, String nickname) {
        String s1 = activity.getString(R.string.invite);
        String s2;
        if (callType == CallType.Vioce) {
            s2 = activity.getString(R.string.make_voice_call);
        } else {
            s2 = activity.getString(R.string.make_video_call);
        }
        if (callAction == CallAction.CALL) {
            this.nickname = s1 + " \"" + nickname + "\" " + s2;
        } else {
            String you = activity.getString(R.string.you);
            this.nickname = "\"" + nickname + "\" " + s1 + " " + you + " " + s2;
        }
    }

    // 拨打方操作 - 挂电话
    public void callerHangup(Activity activity) {
        LogKit.p("拨打方挂电话");
        ImHelpers.setCalling(false);
        String msg = "\"" + App.myNickname + "\" " + App.context.getString(R.string.call_not_connected);
        String localMsgId = ImSendMessageHelper.sendText(MessageType.CallerHangUp, msg, bizId, 0);
        Intent intent = new Intent(BroadCastKey.chattingAppendMsg.name());
        intent.putExtra("localMsgId", localMsgId);
        intent.putExtra("message", msg);
        intent.putExtra("msgType", MessageType.CallerHangUp);
        activity.sendBroadcast(intent);
    }

    // 接听方操作 - 挂电话
    void answerHangup(Activity activity) {
        LogKit.p("接听方挂电话");
        ImHelpers.setCalling(false);
        String msg = "\"" + App.myNickname + "\" " + App.context.getString(R.string.hang_up_call);
        String localMsgId = ImSendMessageHelper.sendText(MessageType.ReceiverHangUp, msg, bizId, 0);
        Intent intent = new Intent(BroadCastKey.chattingAppendMsg.name());
        intent.putExtra("localMsgId", localMsgId);
        intent.putExtra("message", msg);
        intent.putExtra("msgType", MessageType.ReceiverHangUp);
        activity.sendBroadcast(intent);
    }


}