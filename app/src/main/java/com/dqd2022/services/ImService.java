package com.dqd2022.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.dqd2022.Config;
import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.conn.ConnClient;
import com.dqd2022.helpers.App;

import kit.LogKit;

public class ImService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String title = "", content = "";
        int badgeNum = 0;
        if (intent != null) {
            title = intent.getStringExtra("title");
            content = intent.getStringExtra("content");
            badgeNum = intent.getIntExtra("badgeNum", 0);
        }

        // 创建通知并设置内容启动服务
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotification(title, content, badgeNum);
        } else {
            oldNotification(title, content);
        }

        // 启动
        LogKit.p("ImService 保持存活");

        return START_STICKY;
    }

    // Android 8.0+
    private void createNotification(String title, String content, int badgeNum) {
        String CHANNEL_ID = "fly_channel5";
        String CHANNEL_NAME = Config.AppName;
        int id = 1; // 第几条消息，一个 app 在通知栏可多条形成列表
        NotificationChannel channel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // 创建通道
            channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW  // 静默通知
            );
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            if (badgeNum == 0) channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(channel);
            if (badgeNum == 0) notifyManager.cancel(id); // 清除通知、app 角标会被清除

            // 点击通知跳转去哪里
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 通知设置
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setTicker("Nature")
                    .setSmallIcon(R.drawable.im_service_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(content)
                    //.setChannelId(channel.getId())
                    //.setWhen(System.currentTimeMillis()); 定时发送
                    .setNumber(badgeNum)
                    // .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .build();

            notification.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(id, notification);

            // 发送通知，如果需要发送两条不一样的消息，则需要调用两次，传入的 id 需要不一样
            // notifyManager.notify(1, notification);
        }
    }

    // Android 8.0 以下
    private void oldNotification(String title, String content) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "chat")
                        .setSmallIcon(R.drawable.im_service_icon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setVibrate(null) // 不震动
                        .setSound(null)   // 不发声
                        .setLights(0, 0, 0); // 不亮灯
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}