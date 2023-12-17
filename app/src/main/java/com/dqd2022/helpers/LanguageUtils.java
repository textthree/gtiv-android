package com.dqd2022.helpers;

import com.dqd2022.R;

import java.util.Date;

import kit.NumberKit;
import kit.StringKit;
import kit.TimeKit;

public class LanguageUtils {
    /**
     * 将毫秒时间戳转为多久前
     *
     * @param timestamp
     * @return
     */
    static public String Stamp2ago(long timestamp) {
        String ret = App.context.getString(R.string.just);
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
            ret = TimeKit.Timestamp2DateTime(timestamp);
        } else if (diffDays >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffDays))) + " " + App.context.getString(R.string.day_ago);
        } else if (diffHours >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffHours))) + " " + App.context.getString(R.string.hours_ago);
        } else if (diffMins >= 1) {
            ret = NumberKit.toString(StringKit.parseInt(NumberKit.toString(diffMins))) + " " + App.context.getString(R.string.minutes_ago);
        }
        return ret;
    }
}
