package com.dqd2022.page.webrtc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dqd2022.R;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.CallAction;
import com.dqd2022.constant.CallType;
import com.dqd2022.constant.ChatType;
import com.dqd2022.databinding.WebrtcCallActivityBinding;
import com.dqd2022.dto.ContactsItemDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;

import com.dqd2022.model.ContactsModel;

import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.BlurTransformation;
import kit.LogKit;
import kit.MathKit;
import kit.StatusBar.StatusBarKit;
import kit.VibrateKit;

public class CallActivity extends AppCompatActivity {
    private WebrtcCallActivityBinding binding;
    private int callType, callAction, bizId;
    private String nick;
    private String avatar;
    private One2One one2One;
    Timer timer;
    int counter;
    CallActivityBroadcastReciver broadcastReciver;
    CallViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        StatusBarKit.translucentStatus(this);
        binding = DataBindingUtil.setContentView(this, R.layout.webrtc_call_activity);
        vm = new ViewModelProvider(this).get(CallViewModel.class);
        vm.binding = binding;
        binding.setCallModel(vm);
        initData();
        initView();
        setOnClick();
        listenBoracst();
    }

    private void initData() {
        Intent i = getIntent();
        callType = i.getIntExtra("callType", 0);
        callAction = i.getIntExtra("callAction", 0);
        bizId = i.getIntExtra("bizId", 0);
        vm.bizId = bizId;
        ContactsItemDto contacts = new ContactsModel().getOne(ChatType.Private, bizId);
        avatar = contacts.getAvatar();
        nick = contacts.getNickname();
        vm.avatar = contacts.getAvatar();
        vm.callType = callType;
        vm.callAction = callAction;
        vm.setNickname(this, nick);
    }


    // 初始化
    private void initView() {
        RequestBuilder builder;
        // 背景图模糊处理
        RequestOptions options = RequestOptions.bitmapTransform(new BlurTransformation(40, 10));
        RequestManager manager = Glide.with(this);
        if (avatar.equals("")) {
            builder = manager.load(R.drawable.default_avatar);
        } else {
            builder = manager.load(avatar);
        }
        builder.apply(options).into(binding.webrtcCallBackground);

        // 头像圆角
        options = new RequestOptions().transform(new RoundedCorners(MathKit.dp2px(this, 80)));
        manager = Glide.with(this);
        if (avatar.equals("")) {
            builder = manager.load(R.drawable.default_avatar);
        } else {
            builder = manager.load(avatar);
        }
        builder.apply(options).into(binding.webrtcCallAvatar);

        if (callAction == CallAction.CALL) {
            // 拨打方
            binding.webrtcCallHangup.setVisibility(View.VISIBLE);
            one2One = new One2One(this, App.myUserId, bizId, CallAction.CALL, callType);
        } else {
            // 接听方
            long[] pattern = {0, 150, 250, 150};
            VibrateKit.vibrate(this, pattern, 0);
            binding.webrtcAnswer.setVisibility(View.VISIBLE);
            binding.webrtcAnswerHangup.setVisibility(View.VISIBLE);
        }

        // 无论拨号方还是接听方，60 秒未接通则自动挂断
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (counter >= 60) {
                    stopTimer();
                    close();
                    finish();
                    return;
                }
                counter++;
            }
        }, 0, 1000);
    }

    void setOnClick() {
        // 拨打方操作 - 挂电话
        binding.webrtcCallHangup.setOnClickListener(v -> {
            vm.callerHangup(this);
            finish();
        });
        // 接听方操作 - 挂电话
        binding.webrtcAnswerHangup.setOnClickListener(v -> {
            vm.answerHangup(this);
            finish();
        });
        // 接听方 - 接电话
        binding.webrtcAnswer.setOnClickListener(v -> {
            stopTimer();
            VibrateKit.cancel();
            runOnUiThread(() -> {
                one2One = new One2One(this, App.myUserId, bizId, CallAction.ANSWER, callType);
                binding.webrtcAnswer.setVisibility(View.GONE);
                if (callType == CallType.Video) {
                    binding.videoCallSurfaceBig.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    // 销毁定时器
    public void stopTimer() {
        LogKit.p("取消定时器");
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            counter = 0;
        }
    }

    private void close() {
        VibrateKit.cancel();
        stopTimer();
        if (one2One != null) one2One.destroy();
    }


    private class CallActivityBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(BroadCastKey.closeCallActivity.name())) {
                VibrateKit.cancel();
                finish();
            }
        }
    }

    // 监听广播
    private void listenBoracst() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadCastKey.closeCallActivity.name()); // 指定要监听的自定义广播
        broadcastReciver = new CallActivityBroadcastReciver();
        registerReceiver(broadcastReciver, filter);
    }

    @Override
    protected void onDestroy() {
        if (broadcastReciver != null) unregisterReceiver(broadcastReciver);
        LogKit.p("CallActivity onDestroy");
        ImHelpers.calling = false;
        close();
        super.onDestroy();
    }

}