package com.dqd2022.api;

import android.util.Log;

import com.dqd2022.helpers.App;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import kit.LogKit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {
    private Retrofit retrofit;
    boolean hasException;

    public Retrofit builder() {
        return retrofit;
    }

    public RetrofitBuilder(String gateway) {
        // Retrofit 的 baseUlr 必须以 /（斜杠）结束，不然会抛 IllegalArgumentException。
        gateway = gateway + "/";

        // retrofit 官网：https://square.github.io/retrofit/
        // 使用 okhttp 拦截器创建 retrofit
        // 拦截器创建可 add 多个：addInterceptor(new LoggingInterceptor())
        OkHttpClient okHttpClient = new OkHttpClient.Builder().
                addInterceptor(new ApiInterceptor()).
                connectTimeout(10, TimeUnit.SECONDS).
                readTimeout(10, TimeUnit.SECONDS).
                writeTimeout(10, TimeUnit.SECONDS).
                build();
        // GsonConverterFactory 用于将后端返回的 json 扫描到 java 属性中
        Retrofit.Builder builder = new Retrofit.Builder().client(okHttpClient).
                addConverterFactory(GsonConverterFactory.create()).baseUrl(gateway);
        retrofit = builder.build();
    }


    // 自定义拦截器，
    public class ApiInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String authorization = App.token != null ? App.token : "";
            String lastLoginTime = App.lastLoginTime != null ? App.lastLoginTime.toString() : "0";
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", authorization)
                    .addHeader("LastLoginTime", lastLoginTime)
                    .addHeader("LanguageCode", App.language)
                    .build();
            Response response = chain.proceed(request);
            return response;
        }
    }

    // 日志拦截器
    class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.d("logkit OkHttp", String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.d("logkit OkHttp", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }


}
