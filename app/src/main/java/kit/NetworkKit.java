package kit;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;

/**
 * 需要权限：<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
 */
public class NetworkKit {

    // 允许主线程同步请求网络，同步请求捕获异常很困难，在主线程发生异常会导致程序崩溃
    // 主线程进行网络请求如果网络请求超时会导致主线程卡死，而主线程卡死操作系统会杀掉 app
    // 对应频繁访问的接口更加不要在主线程上做同步请求，这就是颗手雷
    public static void allowMainThreadSync() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    /**
     * 判断网络连接是否可用:true 表示网络可用，false 不可用
     *
     * @return
     */
    public static boolean checkNetworkAvailable(Context ctx) {
        // 获取手机所有链接管理对象（包括对Wi-Fi，net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 新版本调用方法获取网络状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if (connectivityManager == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if (info != null && info.length > 0) {
                    for (int i = 0; i < info.length; i++) {
                        LogKit.p(i + "状态" + info[i].getState());
                        LogKit.p(i + "类型" + info[i].getTypeName());
                        // 判断当前网络状态是否为连接状态
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


}
