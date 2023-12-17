package com.dqd2022.page.video;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dqd2022.api.VideosApi;
import com.dqd2022.constant.CachePath;
import com.dqd2022.dto.VideoHomeListDto;
import com.dqd2022.dto.VideoPlaylistItemDto;
import com.dqd2022.helpers.CacheHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kit.FileKit;
import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoViewModel extends ViewModel {
    Context context;
    private final MutableLiveData<String> mText;
    HashMap<Integer, Boolean> cacheTask;

    public VideoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is video fragment");
        cacheTask = new HashMap<>();
    }

    public LiveData<String> getText() {
        return mText;
    }


    interface getVideoListCallback {
        void call(int count);
    }

    // 拉取视频
    public void getVideoList(getVideoListCallback callback, ArrayList<VideoPlaylistItemDto> playlist) {
        VideosApi.getInstance().homeList().enqueue(new Callback<VideosApi.HomeVideoResponse>() {
            @Override
            public void onResponse(Call<VideosApi.HomeVideoResponse> call, Response<VideosApi.HomeVideoResponse> response) {
                if (!response.isSuccessful()) return;
                VideosApi.HomeVideoResponse res = response.body();
                List<VideoHomeListDto> list = res.List;
                String endpoint = res.Endpoint;
                if (res.ApiCode != 0) {
                    return;
                }
                Iterator iterator = list.iterator();
                //item = new VideoPlaylistItemDto();
                //item.setCover("https://text3cn.oss-cn-shenzhen.aliyuncs.com/test/cover.png");
                //item.setVideoUri("https://text3cn.oss-cn-shenzhen.aliyuncs.com/test/index.m3u8");
                //playlist.add(item);
                // 第一张同步下载。应该改为全部异步，主线程等待子线程
                int i = 0;
                while (iterator.hasNext()) {
                    VideoHomeListDto it = (VideoHomeListDto) iterator.next();
                    VideoPlaylistItemDto item = makePlaylistItem(endpoint, it, i);
                    playlist.add(item);
                    i++;
                }
                i = 0;
                callback.call(1);
            }

            @Override
            public void onFailure(Call<VideosApi.HomeVideoResponse> call, Throwable t) {
                LogKit.p("获取视频列表失败了", t.getMessage());
                callback.call(0);
            }
        });
    }

    // 异步拉取视频
    public void getVideoListAsync(ArrayList<VideoPlaylistItemDto> playlist) {
        VideosApi.getInstance().homeList().enqueue(new Callback<VideosApi.HomeVideoResponse>() {
            @Override
            public void onResponse(Call<VideosApi.HomeVideoResponse> call, Response<VideosApi.HomeVideoResponse> response) {
                VideosApi.HomeVideoResponse res = response.body();
                List<VideoHomeListDto> list = res.List;
                String endpoint = res.Endpoint;
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    VideoHomeListDto it = (VideoHomeListDto) iterator.next();
                    VideoPlaylistItemDto item = makePlaylistItem(endpoint, it);
                    playlist.add(item);
                }
            }

            @Override
            public void onFailure(Call<VideosApi.HomeVideoResponse> call, Throwable t) {
                LogKit.p("异步获取视频列表失败");
                t.printStackTrace();
            }
        });
    }

    // 组装 play item
    VideoPlaylistItemDto makePlaylistItem(String endpoint, VideoHomeListDto it, int... index) {
        VideoPlaylistItemDto item = new VideoPlaylistItemDto();
        String video = endpoint + it.Uri;
        String cover;
        // 冷启动时同步下载一张图片，直接使用网络图片在没加载出来前 surfaceview 处于黑屏状态
        CacheHelpers fileHelpers = new CacheHelpers();
        if (index.length > 0 && (index[0] == 0)) {
            cover = fileHelpers.downloadVideoCover(context, endpoint + it.Cover, false);
        } else {
            cover = fileHelpers.downloadVideoCover(context, endpoint + it.Cover, true);
        }
        item.setVideoId(it.Id);
        item.setVideoUri(video);
        item.setCover(cover);
        item.setWidth(it.Width);
        item.setHeight(it.Height);
        item.setUserId(it.UserId);
        item.setTitle(it.Title);
        item.setNickname(it.Nickname);
        item.setAvatar(context, it.Avatar);
        item.setFollow(it.IsFollow);
        item.setSupportNum(it.SupportNum);
        item.setCollectNum(it.CollectNum);
        item.setShareNum(it.ShareNum);
        return item;
    }


    // 封面图等比调整宽高，宽度始终百分百，用宽度计算缩放比
    int[] coverFullWidth(int width, int height) {
        float targetH, targeW;
        DisplayMetrics dp = Resources.getSystem().getDisplayMetrics();
        targeW = dp.widthPixels;
        float scaling = targeW / width;
        targetH = height * scaling;
        return new int[]{(int) targeW, (int) targetH};
    }

    void stopCacheTask(int position) {
        cacheTask.put(position, false);
    }

    // 缓存视频
    void cacheVideo(int position, String videoUrl) {
        cacheTask.put(position, true);
        Thread T = new Thread(() -> {
            String[] seg = videoUrl.split("/");
            String dir = CachePath.video(context) + seg[seg.length - 2] + "/";
            String filename = seg[seg.length - 1];
            String cacheFilePath = dir + filename;
            String cachedMarkFile = dir + "cached";
            if (!FileKit.exists(cachedMarkFile)) {
                LogKit.p("进行缓存:", videoUrl);
                FileKit.cacheFile(videoUrl, cacheFilePath);
                // 根据 m3u8 文件缓存切片
                try {
                    String lastLine = FileKit.readLastLine(cacheFilePath, 2);
                    if (lastLine.equals("")) {
                        return;
                    }
                    String num = lastLine.replace("index", "").replace(".ts", "");
                    int lastTs = Integer.valueOf(num);
                    for (int i = 0; i <= lastTs; i++) {
                        filename = "index" + i + ".ts";
                        String tsUrl = videoUrl.replace("index.m3u8", filename);
                        cacheFilePath = dir + filename;
                        if (!cacheTask.get(position)) {
                            LogKit.p("放弃下载任务", position);
                            return;
                        }
                        if (FileKit.exists(cacheFilePath)) {
                            LogKit.p("跳过已存在切片", cacheFilePath);
                            continue;
                        }
                        LogKit.p("下载切片", cacheFilePath);
                        FileKit.cacheFile(tsUrl, cacheFilePath);
                    }
                    // 用于标识是否缓存完成的文件
                    FileKit.filePutContent(cachedMarkFile);
                } catch (Exception e) {
                    LogKit.p("file read error:", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        T.start();
    }

}