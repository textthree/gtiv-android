package kit;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberKit {

    // int 转字符串
    public static String toString(int numberc) {
        return Integer.toString(numberc);
    }

    // long 转字符串
    public static String toString(long numberc) {
        return Long.toString(numberc);
    }

    // 将数字格式为带单位的字符串，中文为多少万，其他为多少 k
    public static String formatWithUnit(String language, int count) {
        float k = 0;
        DecimalFormat formater = new DecimalFormat("#.0");
        formater.setRoundingMode(RoundingMode.FLOOR);
        if (language.equals("zh")) {
            if (count > 10000) {
                k = count / 10000f;
                return formater.format(k) + "万";
            }
            return String.valueOf(count);
        } else {
            if (count > 1000) {
                k = count / 1000f;
                return formater.format(k) + "k";
            }
            return String.valueOf(count);
        }
    }

}
