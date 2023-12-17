package com.dqd2022.services;// package com.rustfisher.tutorial2020.service.floating;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.dqd2022.R;

import kit.LogKit;


/**
 * 悬浮窗的服务
 *
 * @date 2022-01-05 23:53
 */
public class OverlayWindowService extends Service {

    private WindowManager windowManager;
    private View floatView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogKit.p("onStartCommand , " + startId);
        if (floatView == null) {
            LogKit.p("onStartCommand: 创建悬浮窗");
            initUi();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogKit.p("onDestroy");
        super.onDestroy();
    }

    private void initUi() {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.webrtc_lock_screen_activity, null);
        int layoutType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_TOAST;
        }

        WindowManager.LayoutParams floatLp = new WindowManager.LayoutParams(
                (int) (width * (0.9f)),
                (int) (height * (0.1f)),
                layoutType,
                // 如果不设置这个 FLAG_NOT_FOCUSABLE，则弹窗之外的地方无法点击
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );


        floatLp.gravity = Gravity.CENTER;
        floatLp.x = 0;
        floatLp.y = 0;

        // 设置视图背景透明，值为 0-255，值越大越不透明
        floatView.findViewById(R.id.overlay_window_text).getBackground().setAlpha(210);

        // 显示悬浮窗
        windowManager.addView(floatView, floatLp);

        // 监听悬浮窗内点击事件
        floatView.findViewById(R.id.activity_recive_call).setOnClickListener(v -> {
            stopSelf();
            windowManager.removeView(floatView);
            //Intent backToHome = new Intent(getApplicationContext(), RnMainActivity.class);
            //backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(backToHome);
        });


    }
}
