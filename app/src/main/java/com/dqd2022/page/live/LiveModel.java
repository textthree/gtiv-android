package com.dqd2022.page.live;

import com.dqd2022.api.LiveApi;
import com.dqd2022.api.UsersApi;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.dto.LiveListItemDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveModel {


    public LiveModel() {
    }

    public interface Func {
        void call(List<LiveListItemDto> list, String Hls, String rtmp);
    }

    public void getList(Func fn) {
        LiveApi.getInstance().liveList().enqueue(new Callback<LiveApi.LiveListResponse>() {
            @Override
            public void onResponse(Call<LiveApi.LiveListResponse> call, Response<LiveApi.LiveListResponse> response) {
                LiveApi.LiveListResponse res = response.body();
                List<LiveListItemDto> list = res.List;
                fn.call(list, res.Hls, res.Rtmp);
            }

            @Override
            public void onFailure(Call<LiveApi.LiveListResponse> call, Throwable t) {

            }
        });
    }

    // 关注
    public void followOrUnfollw(int userId) {
        UsersApi.getInstance().follow(userId).enqueue(new Callback<CommonResDto>() {
            @Override
            public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {

            }

            @Override
            public void onFailure(Call<CommonResDto> call, Throwable t) {

            }
        });
    }


}