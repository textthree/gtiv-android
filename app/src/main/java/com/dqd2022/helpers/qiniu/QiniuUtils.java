package com.dqd2022.helpers.qiniu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.api.API;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.helpers.App;
import com.qiniu.android.storage.UploadManager;

import org.openapitools.client.models.T3imapiv1ClientTokenRes;

import java.io.IOException;

import kit.LogKit;
import kit.StringKit;
import kit.TimeKit;
import retrofit2.Response;

public class QiniuUtils {
    static UploadManager uploadManager;

    public static String appUpload(String filePath, String mime, String objectKey) {
        return upload(1, filePath, mime, objectKey);
    }

    public static String appUpload(String filePath, String mime) {
        String suffix = '.' + mime.split("/")[1];
        String objectKey = TimeKit.Date() + '/' + StringKit.uuid() + suffix;
        return upload(1, filePath, mime, objectKey);
    }

    public static String imChatUpload(String filePath, String mime) {
        String suffix = '.' + mime.split("/")[1];
        String objectKey = TimeKit.Date() + '/' + StringKit.uuid() + suffix;
        return upload(2, filePath, mime, objectKey);
    }

    // type 1.app 2.chatting (7 天过期的桶)
    private static String upload(int type, String filePath, String mime, String objectKey) {
        String token = "", endpoint = "";
        // 演示版本，用的同一个桶
        String cachekey = type == 1 ? MMKVkey.qiniuToken.name() : MMKVkey.qiniuToken.name();
        String data = App.mmkv.getString(cachekey, "");
        boolean tokenInvalid = true; // 假设 token 失效
        if (!data.equals("")) {
            JSONObject obj = JSON.parseObject(data);
            Long expire = obj.getLongValue("expire");
            if (expire - TimeKit.nowSecond() > 60) {
                // 没过期
                tokenInvalid = false;
                token = obj.getString("token");
                endpoint = obj.getString("endpoint");
            }
        }
        if (tokenInvalid) {
            try {
                //NetworkKit.allowMainThreadSync();
                java.math.BigDecimal _type = new java.math.BigDecimal(2);
                Response<T3imapiv1ClientTokenRes> response = new API().getDefault().obstoreClientTokenGet(_type, "").execute();
                T3imapiv1ClientTokenRes res = response.body();
                if (res.getApiCode() != 0) {
                    LogKit.p("获取 七 🐂 Token 失败, ApiMessage", res.getApiMessage());
                    return "";
                }
                token = res.getImObsToken();
                endpoint = res.getRemoteBaseUrl();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token", token);
                jsonObject.put("endpoint", endpoint);
                jsonObject.put("expire", TimeKit.nowSecond() + 7000);
                App.mmkv.putString(cachekey, jsonObject.toString());
                LogKit.p("获取七 🐂", response, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 上传
        init();
        String path = "";
        filePath = filePath.replace("file://", "");
        uploadManager.put(filePath, objectKey, token, (objkey, info, response) -> {
            // res 包含 hash、key 等信息，具体字段取决于上传策略的设置
//            if (info.isOK()) {
//                LogKit.p("qiniu", "Upload Success");
//            } else {
//                LogKit.p("qiniu", "Upload Fail");
//                //如果失败，这里可以把 info 信息上报自己的服务器，便于后面分析上传错误原因
//            }
            LogKit.p("qiniu", objkey + ",\r\n " + info + ",\r\n ");
        }, null);
        //LogKit.p("最后", endpoint + "/" + objectKey);
        return endpoint + "/" + objectKey;
    }

    private static void init() {
        if (uploadManager == null) {
            uploadManager = new UploadManager();
        }
    }

}
