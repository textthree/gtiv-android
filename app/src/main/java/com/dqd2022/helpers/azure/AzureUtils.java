/*
package com.dqd2022.helpers.azure;


// implementation 'com.microsoft.azure.android:azure-storage-android:0.8.0@aar'
//EAzureBlobStorageFile azure = new EAzureBlobStorageFile(
//        App.context,
//        an,
//        ak,
//        cn,
//        false // SAS Token
//        );
//        String path = azure.uploadFile(filePath, objectKey, mime);
//        return url + path;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.api.ImbizApi;
import com.dqd2022.dto.AppApiDto;
import com.dqd2022.helpers.App;

import java.io.IOException;

import kit.LogKit;
import kit.StringKit;
import kit.TimeKit;
import retrofit2.Response;

public class AzureUtils {

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
        String ak = null, an = null, cn = null, url = null;
        String key = type == 1 ? "azureAppToken" : "azureChatToken";
        String data = App.mmkv.getString(key, "");
        boolean tokenInvalid = true; // 假设 token 失效
        if (!data.equals("")) {
            JSONObject obj = JSON.parseObject(data);
            Long expire = obj.getLongValue("expire");
            if (expire - TimeKit.nowSecond() > 60) {
                // 没过期
                tokenInvalid = false;
                ak = obj.getString("ak");
                an = obj.getString("an");
                cn = obj.getString("cn");
                url = obj.getString("url");
            }
        }
        if (tokenInvalid) {
            try {
                //NetworkKit.allowMainThreadSync();
                Response<AppApiDto.AzureUpKey> response = ImbizApi.getInstance().azureUpKey(type).execute();
                LogKit.p("response", response);
                AppApiDto.AzureUpKey res = response.body();
                if (res.ApiCode != 0) {
                    LogKit.p("获取 Azure 失败, ApiMessage", res.ApiMessage);
                    return "";
                }
                ak = res.AK;
                an = res.AN;
                cn = res.CN;
                url = res.URL;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ak", res.AK);
                jsonObject.put("an", res.AN);
                jsonObject.put("cn", res.CN);
                jsonObject.put("url", res.URL);
                jsonObject.put("expire", TimeKit.nowSecond() + 3600 * 12);
                App.mmkv.putString(key, jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 上传
        EAzureBlobStorageFile azure = new EAzureBlobStorageFile(
                App.context,
                an,
                ak,
                cn,
                false // SAS Token
        );
        String path = azure.uploadFile(filePath, objectKey, mime);
        return url + path;
    }
}
*/
