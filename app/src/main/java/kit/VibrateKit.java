package kit;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Vibrator;

public class VibrateKit {
    static volatile Vibrator vibrator;

    static public void messageVibrate(Context ctx) {
        long[] pattern = {0, 150, 400, 150};
        Integer repeat = -1;
        vibrate(ctx, pattern, repeat);
    }

    /**
     * 消息震动
     *
     * @param activity
     * @param pattern
     * @param repeat   -1.不重复 0.一直重复
     */
    static public void vibrate(Context activity, long[] pattern, int repeat) {
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.cancel();
        }
        // 兼容高版本后台震动
        AudioAttributes audioAttributes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM) //key
                    .build();
            vibrator.vibrate(pattern, repeat, audioAttributes);
        } else {
            vibrator.vibrate(pattern, repeat);
        }
    }

    // 取消震动，如果关联的 Activity 被 finish() 不会自动停止震动
    public static void cancel() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }
}
