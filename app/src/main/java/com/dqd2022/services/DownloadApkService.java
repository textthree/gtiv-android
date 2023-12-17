package com.dqd2022.services;

import android.util.Log;

import com.dqd2022.constant.CommonConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import kit.LogKit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadApkService {

    private static OkHttpClient okHttpClient;

    public static void download(final String url, final String fileName, final UpdateCallback callback) {
        Log.e("url==", url + "");
        Request request = new Request.Builder()
                .addHeader("Accept-Encoding", "identity")
                .url(url).build();
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
        }

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    callback.onFailure();
                    return;
                }

                File filePath = new File(CommonConstants.DOWNLOAD_PATH);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                LogKit.p("filePath:", filePath + "");

                long contentLength = response.body().contentLength();
                byte[] buffer = new byte[1024];
                File file = new File(filePath.getCanonicalPath(), fileName);
                try (InputStream is = response.body().byteStream();
                     FileOutputStream fos = new FileOutputStream(file)) {
                    LogKit.p("保存路径：", file + "");

                    int length;
                    long sum = 0;
                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                        sum += length;
                        int progress = (int) (sum * 1.0f / contentLength * 100);
                        callback.onProgress(progress);
                    }
                    fos.flush();

                    callback.onSuccess();
                } catch (Exception e) {
                    LogKit.p("error:", e.getMessage());
                    callback.onFailure();
                }
            }
        });
    }

    public interface UpdateCallback {

        void onSuccess();

        void onProgress(int progress);

        void onFailure();
    }
}