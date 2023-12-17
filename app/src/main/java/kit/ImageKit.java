package kit;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ImageKit {
    /**
     * 将 bitmap 保存为文件
     *
     * @param mContext
     * @param bm
     * @param targetDir 存储目录
     * @param suffix    文件后缀，如：jpg/png/gif
     */
    public static String saveBitmap(Context mContext, Bitmap bm, String targetDir, String suffix) {
        File file = new File(targetDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = UUID.randomUUID().toString() + "." + suffix;
        File saveFile = new File(targetDir, fileName);
        try {
            FileOutputStream saveImgOut = new FileOutputStream(saveFile);
            // 压缩
            bm.compress(Bitmap.CompressFormat.JPEG, 90, saveImgOut);
            // 存储完成后需要清除相关的进程
            saveImgOut.flush();
            saveImgOut.close();
            //LogKit.p("Save Bitmap", "The picture is save to your phone!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // LogKit.p(saveFile);
        return "file://" + saveFile.getAbsolutePath();
    }

    public static Uri drawable2uri(Context ctx, int resId) {
        Resources r = ctx.getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(resId) + "/"
                + r.getResourceTypeName(resId) + "/"
                + r.getResourceEntryName(resId));
        return uri;
    }


    // 获取图片宽高，BitmapFactory.Options() 方式获取图片元信息，不用把图片加载进内存
    // 资源文件 (drawable/mipmap/raw)
    public static int[] getImageWHbyResId(Context ctx, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeResource(ctx.getResources(), resId, options);
        options.inJustDecodeBounds = true; // 这个参数设置为 true 才有效，
        int w = options.outHeight;
        int h = options.outWidth;
        LogKit.p(w, h);
        return new int[]{w, h};
    }

    // 网络文件。不能在主线程请求网络，会报：NetworkOnMainThreadException
    public static int[] getImageWHbyUrl(String url) {
        InputStream is = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                is = inputStream;
            }
        } catch (IOException e) {
            LogKit.p("获取网络图片出现异常，图片路径为：" + url);
            e.printStackTrace();
        }
        BitmapFactory.decodeStream(is);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int w = options.outHeight;
        int h = options.outWidth;
        return new int[]{w, h};
    }

    // assets，如：bitmap.png
    public static int[] getImageWHbyAssets(Activity activity, String assets) {
        InputStream is = null;
        try {
            is = activity.getAssets().open(assets);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BitmapFactory.decodeStream(is);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 这个参数设置为 true 才有效，
        int w = options.outHeight;
        int h = options.outWidth;
        return new int[]{w, h};
    }

    // 内存卡文件，如：/sdcard/bitmap.png
    public static int[] getImageWHbySdcard(String file) {
        BitmapFactory.decodeFile(file);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 这个参数设置为 true 才有效，
        int w = options.outHeight;
        int h = options.outWidth;
        return new int[]{w, h};
    }


}