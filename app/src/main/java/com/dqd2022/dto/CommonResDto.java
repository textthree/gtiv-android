package com.dqd2022.dto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonResDto {

    public int ApiCode;
    public String ApiMessage;

    public static Callback<CommonResDto> commonCallback = new Callback<CommonResDto>() {
        @Override
        public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {
            // 通用就是无需处理返回结果
        }

        @Override
        public void onFailure(Call<CommonResDto> call, Throwable t) {
            t.printStackTrace();
        }
    };

}


