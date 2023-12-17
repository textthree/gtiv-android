import android.content.res.Resources;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Debug {
    // 3、来运行一把
    static public void main(String[] args) {
        thumbScale(2340, 1080);
    }

    public static int[] thumbScale(int w, int h) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        double dMaxW = new BigDecimal((double) screenWidth / 0.5).setScale(1, RoundingMode.HALF_UP).doubleValue();
        int maxW = (int) dMaxW;
        int maxH = 480;
        int[] ret = new int[2];
        // 宽高都没有超过 320 的直接使用原始大小
        if (w < maxW && h < maxH) {
            ret[0] = w;
            ret[1] = h;
            return ret;
        }
        BigDecimal bigW = new BigDecimal(w);
        BigDecimal bigH = new BigDecimal(h);
        double rate = bigW.divide(bigH, 2, RoundingMode.HALF_UP).doubleValue();
        // 正方形
        if (rate == 1 || rate == 0) {
            ret[0] = ret[1] = maxW;
        }
        // 横图，最大宽度固定，等比缩放宽度
        else if (rate > 1 && w >= maxW) {
            ret[0] = maxW;
            ret[1] = (int) Math.ceil(h / (w / maxW));
        }
        // 竖图，最大高度固定，等比缩放宽度
        else if (rate < 1 && h >= maxH) {
            ret[1] = maxH;
            ret[0] = (int) Math.ceil(w / (h / maxH));
        }
        return ret;
    }
}
