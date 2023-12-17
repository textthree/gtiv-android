package com.dqd2022.constant;

import android.content.Context;

import java.io.File;

import kit.LogKit;

public class CachePath {
    private static void createDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static String chatPhotoDir(Context context) {
        String path = context.getFilesDir() + "/t3im/" + "photo/";
        createDir(path);
        return path;
    }

    // 视频封面
    public static String video(Context context) {
        String path = context.getFilesDir() + "/t3im/" + "video/";
        createDir(path);
        return path;
    }

    public static String chatVoiceDir(Context context) {
        String path = context.getFilesDir() + "/t3im/" + "voice/";
        createDir(path);
        return path;
    }

    // 用户头像之类不需要被清理的缓存数据
    public static String storage(Context context) {
        String path = context.getFilesDir() + "/t3im/" + "storage/";
        createDir(path);
        return path;
    }


    // 通过 rn 调用的下载目前没有做资源分类管理，统一先扔到这个目录
    public static String rnCache(Context context) {
        String path = context.getFilesDir() + "/t3im/" + "rn/";
        createDir(path);
        return path;
    }

}

