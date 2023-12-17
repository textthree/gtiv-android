package com.dqd2022;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.android.tony.defenselib.DefenseCrash;
import com.dqd2022.helpers.App;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.tencent.mmkv.MMKV;

import litepal.LitePal;

import java.util.Locale;

import kit.MmkvKit;

public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 mmkv
        MMKV.initialize(this);
        App.mmkv = MMKV.mmkvWithID(Config.MMKV_ID);
        MmkvKit.init(App.mmkv);

        // 设置语言、全局 context
        Context ctx = getApplicationContext();
        App.setI18n(ctx);
        App.context = ctx;
        Fresco.initialize(this);

        // 初始化 sqllite
        LitePal.initialize(this);

        App.init();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (!Config.Debug) {
            DefenseCrash.initialize(this);
            DefenseCrash.install((thread, throwable, isSafeMode, isCrashInChoreographer) -> {
                // thread: 异常崩溃在的线程对象
                // throwable: 具体的异常对象
                // isSafeMode: 如果应用程序崩溃过,但是被我们捕获,那么这个值将会是true来告知开发人员,
                // 具体来讲就是当你的主线程(Main Looper)已经被错误破坏不能够正常loop的时候,我们将使用魔法保证他运行.这称之为安全模式
                // isCrashInChoreographer: 如果崩溃发生在 OnMeasure/OnLayout/OnDraw 方法中,这将会导致程序白屏或黑屏亦或是一些View显示不成功
                // 当你收到这个值为True的时候,我们建议你关闭或者重启当前的Activity

                // 你当然可以在本方法中抛出异常,但是你的抛出将会被虚拟机(VM)捕获并且你的进程将被它关闭
                Log.i("logkit", "[崩溃啦]" + "thread:${thread.name} " +
                        "exception:${throwable.message} " +
                        "isCrashInChoreographer:$isCrashInChoreographer " +
                        "isSafeMode:$isSafeMode");
                throwable.printStackTrace();
            });
        }
    }
}
