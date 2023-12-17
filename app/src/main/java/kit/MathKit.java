package kit;

import android.content.Context;

public class MathKit {

    // dp 转像素
    public static int dp2px(Context ctx, int dp) {
        int sc = (int) ctx.getResources().getDisplayMetrics().scaledDensity;
        return dp * sc;
    }
}
