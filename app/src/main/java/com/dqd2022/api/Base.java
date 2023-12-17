package com.dqd2022.api;

import com.alibaba.fastjson.JSON;
import com.dqd2022.Config;
import com.dqd2022.helpers.App;

import java.util.HashMap;
import java.util.Map;

import kit.HttpKit;
import kit.LogKit;

public class Base {

    public static Map<String, String> getHeadParams() {
        Map<String, String> headParams = new HashMap<>();
        headParams.put("Authorization", App.token);
        headParams.put("LastLoginTime", App.lastLoginTime.toString());
        return headParams;
    }


    /**
     * 检查 comet 连接状态
     *
     * @return
     */
    public static Boolean checkComet() {
        Boolean ret = false;
        String url = Config.IMBIZ + "/imlogic/check-online";
        try {
            String res = new HttpKit().postJson(url, getHeadParams());
            com.alibaba.fastjson.JSONObject resObj = JSON.parseObject(res);
            ret = resObj.getBoolean("Online");
        } catch (Exception e) {
            LogKit.p("检查 comet 状态失败" + e);
            e.printStackTrace();
        }
        return ret;
    }


}
