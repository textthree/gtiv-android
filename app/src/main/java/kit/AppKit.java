package kit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Locale;


/**
 * 封装的这些方法中的 Context 参数都是 Activity.this
 */
public class AppKit {

    // 判断自己是否运行在前台
    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        if (appProcessInfoList == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 切换 app 前台显示，从任务栏拉出来
     *
     * @param context
     * @param taskId  进程 id ，activity 中通过 getTaskId() 获得
     */
    public static void switchAppForeground(Context context, Integer taskId) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(taskId, 0);
        //activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME);
    }


    // 点亮屏幕
    public static void wakeUpAndUnlock(Context context) {
        //屏锁管理器
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        // 解锁
        kl.disableKeyguard();
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        // 点亮屏幕
        wl.acquire();
        // 释放
        wl.release();
    }

    /**
     * 判断是否已点亮
     *
     * @param context
     * @return
     */
    public static boolean isLockScrren(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return !pm.isInteractive();
    }

    /**
     * 判断手机是否处于密码锁定状态
     *
     * @param context
     * @return
     */
    public static boolean passwordLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = mKeyguardManager.inKeyguardRestrictedInputMode();
        return locked;
    }
 

    /**
     * 收起软键盘
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null && manager != null) {
            manager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 打开软键盘
     *
     * @param activity
     * @param view     EditText 资源
     */
    public static void showKeyBoard(Activity activity, View view) {
        InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);


    }

    // 清除焦点也能触发软键盘收起
    public static void clearFocus(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            focusedView.clearFocus();
        }
    }
}
