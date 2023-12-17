package kit;

import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;


public class HttpKit {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * 添加headers
     *
     * @param headerParams
     * @return
     */
    private Headers setHeaderParams(Map<String, String> headerParams) {
        Headers headers = null;
        Headers.Builder headersbuilder = new Headers.Builder();
        if (headerParams != null && headerParams.size() > 0) {
            for (String key : headerParams.keySet()) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(headerParams.get(key))) {
                    // 如果参数不是 null 并且不是""，就拼接起来
                    headersbuilder.add(key, headerParams.get(key));
                }
            }
        }

        headers = headersbuilder.build();
        return headers;

    }

    // 同步 get 请求
    public String get(String url, Map headParams) {
        Headers headers = setHeaderParams(headParams);
        Request request = new Request.Builder()
                .url(url).get().headers(headers)
                .build();
        String ret = "";
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                LogKit.p("网络请求失败：" + response.body().string());
            }
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }
            ret = response.body().string();
            // LogKit.p("服务器返回：" + ret);
        } catch (IOException e) {
            e.printStackTrace();
            LogKit.p("网络请求失败2" + e);
        }
        return ret;
    }

    // 异步 get 请求


    /**
     * post json
     *
     * @return
     */
    public String postJson(String url, Map headParams, Map... args) {
        headParams.put("Content-Type", "application/json");
        String ret = "";
        // 创建表单请求参数
        FormBody.Builder builder = new FormBody.Builder();
        // 构造 post 参数
        if (args.length > 0) {
            Iterator param = args[0].entrySet().iterator();
            while (param.hasNext()) {
                Object kv = param.next();
                Map.Entry item = (Map.Entry) kv;
                String key = item.getKey().toString();
                String value = item.getValue().toString();
                builder.add(key, value);
            }
        }
        FormBody formBody = builder.build();
        Headers headers = setHeaderParams(headParams);
        Request request = new Request.Builder().url(url).post(formBody).headers(headers).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LogKit.p("postJson 网络请求失败：" + response.body().string());
            }
            Headers responseHeaders = response.headers();
            //for (int i = 0; i < responseHeaders.size(); i++) {
            //System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            //}
            ret = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            LogKit.p("postJson IOException" + e);
        }
        return ret;
    }

    // 异步下载文件
    public String downloadFileAsync(final String url, final String destFileDir, final String... destFileName) {
        String fileName;
        if (destFileName.length > 0) {
            fileName = destFileName[0];
        } else {
            String[] segments = url.split("/");
            fileName = segments[segments.length - 1];
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) {
                saveFile(destFileDir, fileName, response);
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                LogKit.p("[文件下载失败 onFailure]", e);
            }
        });
        return "file://" + destFileDir + fileName;
    }

    // 同步下载文件
    public String downloadFileSync(final String url, final String destFileDir, final String... destFileName) {
        NetworkKit.allowMainThreadSync();
        String fileName;
        if (destFileName.length > 0) {
            fileName = destFileName[0];
        } else {
            String[] segments = url.split("/");
            fileName = segments[segments.length - 1];
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 同步请求
        try {
            Response response = client.newCall(request).execute();
            saveFile(destFileDir, fileName, response);
            return "file://" + destFileDir + fileName;
        } catch (Exception e) {
            LogKit.p("[同步下载文件失败 onFailure] url:", url, "fileName:", fileName, "error:", e);
            return "";
        }
    }

    void saveFile(String destFileDir, String fileName, Response response) {
        //储存下载文件的目录
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        Sink sink = null;
        BufferedSink bufferedSink = null;
        try {
            sink = Okio.sink(file);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.writeAll(response.body().source());
            bufferedSink.close();
        } catch (Exception e) {
            LogKit.p("[文件下载失败 onResponse]", e);
        } finally {
            if (bufferedSink != null) {
                try {
                    bufferedSink.close();
                } catch (IOException e) {
                    LogKit.p("[close bufferedSink fail]", e);
                }
            }
        }
    }

}
