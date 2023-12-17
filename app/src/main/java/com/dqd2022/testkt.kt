package com.dqd2022

import com.dqd2022.api.API
import kit.LogKit
import org.openapitools.client.*
import org.openapitools.client.infrastructure.*
import org.openapitools.client.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class testkt {


    fun run() {
        API().User.userInfoGet("223").enqueue(object : Callback<T3imapiv1UserinfoRes?> {
            override fun onResponse(call: Call<T3imapiv1UserinfoRes?>, response: Response<T3imapiv1UserinfoRes?>) {
                var res = response.body()
                LogKit.p("HHHHH", res?.nickname)
            }

            override fun onFailure(call: Call<T3imapiv1UserinfoRes?>, t: Throwable) {
                LogKit.p("121212121")
            }
        })

    }

}