package com.dqd2022.helpers;

import android.content.Context;

import com.dqd2022.constant.CachePath;

import kit.FileKit;
import kit.HttpKit;

public class CacheHelpers {

    public class CacheRetDto {
        public String localFileUri = "";
        public boolean hasCache = false;
    }

    public static CacheHelpers getInstance() {
        return new CacheHelpers();
    }

    /**
     * 缓存视频封面
     *
     * @param url
     * @param async 是否异步下载
     * @return
     */
    public String downloadVideoCover(Context context, String url, boolean async) {
        String[] seg = url.split("/");
        String ret, fileName;
        String dir = CachePath.video(context) + seg[seg.length - 2] + "/";
        fileName = seg[seg.length - 1];
        if (FileKit.exists(dir, fileName)) {
            ret = "file://" + dir + fileName;
        } else {
            if (async) {
                ret = new HttpKit().downloadFileAsync(url, dir, fileName);
            } else {
                ret = new HttpKit().downloadFileSync(url, dir, fileName);
            }
        }
        return ret;
    }


    /**
     * 缓存用户头像
     * 同步下载失败会原样返回远程 url，
     * 异步下载失败则返回了个本地路径但是文件不存在，因此在取用头像时应该判断文件是否存在，不存在发起异步下载
     *
     * @param context
     * @param url
     * @param async
     * @return
     */
    public CacheRetDto downloadAvatar(Context context, String url, boolean async) {
        CacheRetDto ret = new CacheRetDto();
        if (url.equals("") || !url.startsWith("http")) {
            ret.hasCache = false;
            ret.localFileUri = url;
            return ret;
        }
        String dir, fileName;
        dir = CachePath.storage(context);
        String[] seg = url.split("/");
        fileName = seg[seg.length - 1];
        ret.localFileUri = "file://" + dir + fileName;
        if (FileKit.exists(dir, fileName)) {
            ret.hasCache = true;
            return ret;
        }
        // 下载，下载失败返回空
        String localfile = "";
        if (async) {
            localfile = new HttpKit().downloadFileAsync(url, dir, fileName);
            // 同步下载失败原样返回 url
            if (localfile.equals("")) {
                ret.localFileUri = url;
            }
        } else {
            new HttpKit().downloadFileSync(url, dir, fileName);
        }
        return ret;
    }

    public CacheRetDto downloadAvatar(String url, boolean async) {
        return downloadAvatar(App.context, url, async);
    }

}
