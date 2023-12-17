package com.dqd2022.api

import android.util.Log
import com.dqd2022.helpers.App
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.openapitools.client.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetrofitClient(val gateway: String) {

    fun builder(): Retrofit {
        // Retrofit 的 baseUlr 必须以 /（斜杠）结束，不然会抛 IllegalArgumentException。
        var gateway = gateway
        gateway = "$gateway/"

        // retrofit 官网：https://square.github.io/retrofit/
        // 使用 okhttp 拦截器创建 retrofit
        // 拦截器创建可 add 多个：addInterceptor(new LoggingInterceptor())
        val okHttpClient: OkHttpClient =
            OkHttpClient.Builder().addInterceptor(ApiInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        val serializerBuilder: Moshi.Builder = Serializer.moshiBuilder
        val builder = Retrofit.Builder().client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(serializerBuilder.build())).baseUrl(gateway)
        return builder.build()
    }


    // 自定义拦截器，
    class ApiInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val authorization = if (App.token != null) App.token else ""
            val lastLoginTime = if (App.lastLoginTime != null) App.lastLoginTime.toString() else "0"
            val request: Request = chain.request()
                .newBuilder()
                .addHeader("Authorization", authorization)
                .addHeader("LastLoginTime", lastLoginTime)
                .addHeader("LanguageCode", App.language)
                .build()
            return chain.proceed(request)
        }
    }


}