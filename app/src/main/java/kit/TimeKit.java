package kit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeKit {

    /**
     * 获取当前日期
     */
    static public String Date() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 获取当前时间
     */
    static public String DateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }


    /**
     * 获取当前时间,指定格式化模板
     */
    static public String DateTime(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(new Date());
    }

    static public String Timestamp2DateTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTimeStr = simpleDateFormat.format(date);
        return dateTimeStr;
    }

    /**
     * 将毫秒时间戳转为多久前
     *
     * @param timestamp
     * @return
     */
    static public String Stamp2ago(long timestamp) {
        String ret = "刚刚";
        long now = new Date().getTime();
        long diff = now - timestamp;
        if (diff <= 0) {
            return ret;
        }
        int minute = 1000 * 60;
        int hour = minute * 60;
        int day = hour * 24;
        long diffDays = diff / day;
        long diffHours = diff / hour;
        long diffMins = diff / minute;
        if (diffDays > 7) {
            ret = Timestamp2DateTime(timestamp);
        } else if (diffDays >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffDays))) + " 天前";
        } else if (diffHours >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffHours))) + " 小时前";
        } else if (diffMins >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffMins))) + " 分钟前";
        }
        return ret;
    }

    static public String Stamp2agoEn(long timestamp) {
        String ret = "just";
        long now = new Date().getTime();
        long diff = now - timestamp;
        if (diff <= 0) {
            return ret;
        }
        int minute = 1000 * 60;
        int hour = minute * 60;
        int day = hour * 24;
        long diffDays = diff / day;
        long diffHours = diff / hour;
        long diffMins = diff / minute;
        if (diffDays > 7) {
            ret = Timestamp2DateTime(timestamp);
        } else if (diffDays >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffDays))) + " days ago";
        } else if (diffHours >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffHours))) + " hours ago";
        } else if (diffMins >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffMins))) + " minutes ago";
        }
        return ret;
    }

    // 获取当前毫秒时间戳
    static public Long nowMillis() {
        // 或者 new Date().getTime()
        return System.currentTimeMillis();
    }

    // 获取当前秒时间戳
    static public Long nowSecond() {
        return System.currentTimeMillis() / 1000;
    }
}
