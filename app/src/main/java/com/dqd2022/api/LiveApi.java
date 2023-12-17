package com.dqd2022.api;

import com.dqd2022.Config;
import com.dqd2022.dto.LiveListItemDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class LiveApi {
    Retrofit retrofit;
    private static LiveApi instance;

    public LiveApi() {
        retrofit = new RetrofitBuilder(Config.VideoApi).builder();
    }

    public static LiveApi getInstance() {
        if (instance != null) {
            return instance;
        }
        return new LiveApi();
    }

    //  Response dto
    public class LiveListResponse {
        public int ApiCode;
        public String ApiMessage;
        public String Rtmp;
        public String Hls;
        public List<LiveListItemDto> List;
    }

    // interface
    public interface LiveListService {
        @GET("live/list")
        Call<LiveListResponse> liveList();
    }

    // implement
    public Call<LiveListResponse> liveList() {
        LiveListService service = retrofit.create(LiveListService.class);
        Call<LiveListResponse> call = service.liveList();
        return call;
    }

}
