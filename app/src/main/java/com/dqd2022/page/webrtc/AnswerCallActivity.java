package com.dqd2022.page.webrtc;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.dqd2022.R;
import com.dqd2022.helpers.App;

import java.util.Timer;
import java.util.TimerTask;

import kit.LogKit;
import kit.StatusBar.StatusBarKit;

public class AnswerCallActivity extends AppCompatActivity {

    static public AnswerCallActivity instance;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 不给悬浮窗权限，设置启动模式为 singleInstance 试下能不能弹出来
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        instance = this;
        StatusBarKit.translucentStatus(this);
        StatusBarKit.setFontBlack(this);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |        // 锁屏状态下显示
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |  // 点亮屏幕
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON    // 保持屏幕常亮
        );
        setContentView(R.layout.webrtc_lock_screen_activity);

        View view = findViewById(R.id.activity_recive_call);
        view.getBackground().setAlpha(210);

        view.setOnClickListener(v -> {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissError() {
                    super.onDismissError();
                }

                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    finishCallReciveActivity();
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                }
            });
        });

        // 60 秒关闭
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                finishCallReciveActivity();
            }
        }, 60 * 1000);
    }

    public void finishCallReciveActivity() {
        if (!AnswerCallActivity.this.isDestroyed()) {
            AnswerCallActivity.this.finish();
            LogKit.p("销毁啊 ");
        }
    }

//    @Override
//    public void invokeDefaultOnBackPressed() {
//        super.onBackPressed();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}