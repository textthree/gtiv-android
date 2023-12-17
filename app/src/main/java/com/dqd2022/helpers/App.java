package com.dqd2022.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dqd2022.MainActivity;
import com.dqd2022.R;
import com.dqd2022.api.ImbizApi;
import com.dqd2022.conn.ConnClient;
import com.dqd2022.constant.Language;
import com.dqd2022.constant.MMKVkey;
import com.dqd2022.dto.UserApiJavaDto;
import com.dqd2022.dto.UserinfoRes;
import com.dqd2022.page.im.ImContainterActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.picture.lib.language.LanguageConfig;
import com.tencent.mmkv.MMKV;

import litepal.LitePal;

import java.util.Locale;

import kit.LogKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// mmkv 本身就是内存映射，并且做了内存溢出判断，更安全。使用 mmkv 存储的数据可以不用再写入静态变量
public class App {
    public static Context context;
    public static int myUserId;
    public static String myNickname;
    public static String myUsername;
    public static String myAvatar;
    public static int myCreatVideoNum;
    public static int myCollectVideoNum;
    public static int myContactsVersion;
    public static String token; // 访问服务器的 token
    public static Long lastLoginTime; // 访问服务器需要携带的最后登录时间
    public static String language; // constat/Language.java
    public static int bottomNaviBgBlack = 0xFF2A2A2A;
    public static int bottomNaviBgWhite = 0xFFFFFFFF;
    public static MMKV mmkv;
    public static int fansNum;
    public static int followNum;
    public static int supportNum;
    public static int selectorLanguage = LanguageConfig.ENGLISH; // 相册选择器语言包，默认英文


    // 初始化
    public static void init() {
        cacheI18n();
        loadUserinfo();
        if (isLogin()) {
            getMyUserinfo();
            ImHelpers.locaCache();
            if (ImHelpers.getTotalBadge() > 0) {
                ImHelpers.setImService(ImHelpers.getTotalBadge());
            }
            ImHelpers.checkAddMeList();
            ImHelpers.connClient = new ConnClient();
            ImHelpers.connClient.start();
        }
    }

    private static void cacheI18n(String... languageCode) {
        if (languageCode.length == 0) {
            // 设置默认语言
            language = mmkv.getString("i18n", Language.chinese);
        } else {
            // 设置指定语言
            language = languageCode[0];
        }
        mmkv.putString("i18n", language);
        // 图片选择器语言包
        if (language.equals("zh")) {
            selectorLanguage = LanguageConfig.CHINESE;
        }
    }

    // 这个无法用全局 applicationContext 去设置语言包，需要 activity 中设置才生效
    // 语言默认是跟随系统，这里默认设置为 en
    // 目标语言，可选值可以在 java.util.Locale 包中查看，目前可选值：zh 或 cn，只做了这俩语言包
    public static void setI18n(Context activity, String... languageCode) {
        cacheI18n(languageCode);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }


    // 从服务器拉取自己最新的用户信息
    static void getMyUserinfo() {
        LogKit.p("getMyUserinfogetMyUserinfogetMyUserinfogetMyUserinfogetMyUserinfogetMyUserinfo");
        try {
            ImbizApi.getInstance().getUserinfo(String.valueOf(App.myUserId)).enqueue(new Callback<UserinfoRes>() {
                @Override
                public void onResponse(Call<UserinfoRes> call, Response<UserinfoRes> response) {
                    UserinfoRes res = response.body();
                    if (res == null) {
                        return;
                    }
                    UserApiJavaDto.Userinfo cache = App.getUserinfoCache();
                    cache.Nickname = res.getNickname();
                    if (res.getFansNum() > 0) {
                        cache.FansNum = res.getFansNum();
                    }
                    if (res.getFollowNum() > 0) {
                        cache.FollowNum = res.getFollowNum();
                    }
                    if (res.getSupportNum() > 0) {
                        cache.SupportNum = res.getSupportNum();
                    }
                    cache.CreateVideoNum = res.getCreateVideoNum();
                    cache.CollectVideoNum = res.getCollectVideoNum();
                    ImHelpers.syncContactsAndMessage(res.getContactsVersion());
                    LogKit.p("联系人版本 ",res.getContactsVersion() , "|", App.myContactsVersion);
                    if (res.getContactsVersion() > App.myContactsVersion) {
                        cache.ContactsVersion = res.getContactsVersion();
                    }
                    App.setUserinfoCache(cache);
                    App.loadUserinfo();
                }

                @Override
                public void onFailure(Call<UserinfoRes> call, Throwable t) {
                    LogKit.p("[App.getMyUserinfo onFailure]", t.getMessage());
                }
            });
        } catch (Exception e) {
            LogKit.p("[App.getMyUserinfo Exception]", e.getMessage());
        }
    }

    public static boolean isLogin() {
        return myUserId > 0;
    }

    // 从缓存中加载信息到内存
    public static void loadUserinfo() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = App.mmkv.getString(MMKVkey.userinfo.name(), "");
            if (!jsonStr.equals("")) {
                UserApiJavaDto.Userinfo userinfo = objectMapper.readValue(jsonStr, UserApiJavaDto.Userinfo.class);
                myUserId = userinfo.UserId;
                token = userinfo.Token;
                lastLoginTime = userinfo.LastLoginTime;
                myNickname = userinfo.Nickname;
                myAvatar = userinfo.Avatar;
                fansNum = userinfo.FansNum;
                supportNum = userinfo.SupportNum;
                myCreatVideoNum = userinfo.CreateVideoNum;
                myCollectVideoNum = userinfo.CollectVideoNum;
                followNum = userinfo.FollowNum;
                myUsername = userinfo.Username;
                myContactsVersion = userinfo.ContactsVersion;
            }
        } catch (JsonProcessingException e) {
            LogKit.p("[loadUserinfo] 读取用户信息失败");
        }
    }

    // 从缓存获取用户信息
    public static UserApiJavaDto.Userinfo getUserinfoCache() {
        UserApiJavaDto.Userinfo userinfo = new UserApiJavaDto.Userinfo();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = App.mmkv.getString(MMKVkey.userinfo.name(), "");
            if (!jsonStr.equals("")) {
                userinfo = objectMapper.readValue(jsonStr, UserApiJavaDto.Userinfo.class);
            }
        } catch (JsonProcessingException e) {
            LogKit.p("[getUserinfoCache] 读取用户信息失败");
        }
        return userinfo;
    }

    // 缓存用户信息
    public static void setUserinfoCache(UserApiJavaDto.Userinfo userinfo) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(userinfo);
            App.mmkv.putString(MMKVkey.userinfo.name(), jsonString);
        } catch (JsonProcessingException e) {
            LogKit.p("[setUserinfoCache]", e.getMessage());
        }
    }

    // 更新用户信息中的某个字段
    public static void updateUserinfoField(String field, String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = App.mmkv.getString(MMKVkey.userinfo.name(), "");
            if (!jsonStr.equals("")) {
                UserApiJavaDto.Userinfo userinfo = objectMapper.readValue(jsonStr, UserApiJavaDto.Userinfo.class);
                switch (field) {
                    case "myAvatar":
                        userinfo.Avatar = value;
                        myAvatar = value;
                        break;
                    case "myNickname":
                        userinfo.Nickname = value;
                        myNickname = value;
                        break;
                }
                String jsonString = objectMapper.writeValueAsString(userinfo);
                App.mmkv.putString(MMKVkey.userinfo.name(), jsonString);
            }
        } catch (JsonProcessingException e) {
            LogKit.p("[updateUserinfoField]", e.getMessage());
        }

    }

    // 获取数据库
    public static SQLiteDatabase getDb() {
        return LitePal.getDatabase();
    }

    // 退出登录
    public static void logout(Activity activity) {
        myUserId = 0;
        int bootCount = mmkv.getInt(MMKVkey.userinfo.bootCount.name(), 0);
        mmkv.clear();
        mmkv.putInt(MMKVkey.bootCount.name(), bootCount);
        ImHelpers.connClient.close();
        ImHelpers.connClient = null;
        Intent intent = new Intent(activity, MainActivity.class);
        // 设置标志位，以实现重新启动的方式
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }


    /* fragement 之间导航，调用时传递参数代码 CV ：
       val fragment = EditRoomNameFragment()
       val bundle = Bundle()
       bundle.putString("roomName", vm.getNickname().value)
       fragment.arguments = bundle
     */
    public static void switchFragmentWithAnim(FragmentManager fm, Fragment fragment, int... id) {
        int layoutId = R.id.activity_fragment_container;
        if (id.length > 0) layoutId = id[0];
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_right_in_fragment, R.anim.slide_left_out_fragment,
                R.anim.slide_left_in_fragment, R.anim.slide_right_out_fragment);
        transaction.replace(layoutId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    // 启动 ImContainnerActivity
    public static void startImContainerActiviy(Activity activity, int fragmentKey, Bundle... bundle) {
        Intent intent = new Intent(activity, ImContainterActivity.class);
        intent.putExtra("fragmentKey", fragmentKey);
        if (bundle.length > 0) {
            intent.putExtra("bundle", bundle[0]);
        }
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }


}
