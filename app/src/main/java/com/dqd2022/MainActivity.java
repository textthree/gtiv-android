package com.dqd2022;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.databinding.MainActivityBinding;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.DialogOkFragment;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.page.im.chatlist.ChatlistFragment;
import com.dqd2022.page.live.LiveFragment;
import com.dqd2022.page.login.LoginActivity;
import com.dqd2022.page.mine.MineFragment;
import com.dqd2022.page.video.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;


import kit.ArrayKit;
import kit.LogKit;
import kit.StatusBar.StatusBarKit;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity {
    private MainActivityBinding binding;
    private BottomNavigationView navView;
    HashMap<Integer, Fragment> fragments;
    private int visibledFragmentKey;  // 当前显示的 Fragment
    private static MainActivity mainActivity;
    private BroadcastReceiver broadcastReceiver;

    public static MainActivity getInstance() {
        return mainActivity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        StatusBarKit.translucentStatus(this);
        App.setI18n(this);
        // 底部菜单
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navView = binding.navView;
        navView.setItemIconTintList(null); // 点击图标不要变色
        navView.setItemIconSize(85); // MathKit.dp2px(this, 30)
        navView.setBackgroundColor(App.bottomNaviBgBlack);
        getWindow().setNavigationBarColor(App.bottomNaviBgBlack); // 底部虚拟按键背景
        // 初始化 Fragment
        initFragment();
        debugInit();
        refreshBadge();
        navView.setOnItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.bottom_menu_video:
                    switchFragment(FragmentKey.video);
                    return true;
                case R.id.bottom_menu_live:
                    switchFragment(FragmentKey.live);
                    return true;
                case R.id.bottom_menu_message:
                    if (checkLogin()) {
                        switchFragment(FragmentKey.chatlist);
                        requestCameraPermissions();
                        return true;
                    }
                    return false;
                case R.id.bottom_menu_mine:
                    if (checkLogin()) {
                        switchFragment(FragmentKey.mine);
                        return true;
                    }
                    return false;
            }
            return false;
        });
        // 注册个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadCastKey.checkAddMeFinish.name());
        filter.addAction(BroadCastKey.syncMessageFinish.name());
        filter.addAction(BroadCastKey.refreshBadge.name());
        filter.addAction(BroadCastKey.otherPlaceLogin.name());
        broadcastReceiver = new Broadcast();
        registerReceiver(broadcastReceiver, filter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (visibledFragmentKey == FragmentKey.video) {
            setDarkTheme();
        } else if (visibledFragmentKey == FragmentKey.chatlist) {
            StatusBarKit.setBgWhiteAndFontBlack(this);
        } else if (visibledFragmentKey == FragmentKey.mine) {
            StatusBarKit.translucentStatus(this);
            StatusBarKit.setFontWhite(this);
        }
        LogKit.p("MainActiviy onResume", "visibledFragmentKey:", visibledFragmentKey);
    }

    @Override
    public void onBackPressed() {
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo =
                pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        ActivityInfo ai = homeInfo.activityInfo;
        Intent startIntent = new Intent(Intent.ACTION_MAIN);
        startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
        startActivity(startIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);
    }

    // 初始化 Fragment 栈
    private void initFragment() {
        fragments = new HashMap();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment videoFragment = new HomeFragment();
        Fragment liveFragment = new LiveFragment();
        fragments.put(FragmentKey.video, videoFragment);
        fragments.put(FragmentKey.live, liveFragment);
        int id = binding.mainActivityFragment.getId();
        transaction.add(id, liveFragment).hide(liveFragment);
        transaction.add(id, videoFragment);
        visibledFragmentKey = FragmentKey.video;
        if (App.isLogin()) {
            initLoginedFragments();
        }
        transaction.commit();

    }


    // 初始化已登录可见的页面
    void initLoginedFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment messageFragment = new ChatlistFragment();
        Fragment mineFragment = new MineFragment();
        fragments.put(FragmentKey.chatlist, messageFragment);
        fragments.put(FragmentKey.mine, mineFragment);
        transaction.commit();
    }


    // 切换 Fragment
    public void switchFragment(int fragmentKey) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(fragments.get(visibledFragmentKey));
        Fragment target = fragments.get(fragmentKey);
        if (target == null) {
            if (fragmentKey == FragmentKey.chatlist) {
                target = new ChatlistFragment();
                fragments.put(FragmentKey.chatlist, target);
            } else if (fragmentKey == FragmentKey.mine) {
                target = new MineFragment();
                fragments.put(FragmentKey.mine, target);
            }
        }
        if (!target.isAdded()) {
            transaction.add(binding.mainActivityFragment.getId(), target).show(target);
        } else {
            transaction.show(target);
        }
        transaction.commit();
        visibledFragmentKey = fragmentKey;
        // 改变状态栏、底部菜单栏、虚拟按键背景
        if (fragmentKey == FragmentKey.video) {
            StatusBarKit.setBgBlackAndFontWhite(this);
            navView.setBackgroundColor(App.bottomNaviBgBlack);
            getWindow().setNavigationBarColor(App.bottomNaviBgBlack);
        } else if (fragmentKey == FragmentKey.mine) {
            StatusBarKit.translucentStatus(this, false);
            navView.setBackgroundColor(App.bottomNaviBgWhite);
            getWindow().setNavigationBarColor(App.bottomNaviBgWhite);
        } else {
            setLightTheme();
        }
        // 显示底部菜单
        int[] hideNav = new int[]{FragmentKey.splash};
        if (ArrayKit.inArray(fragmentKey, hideNav)) {
            navView.setVisibility(View.GONE);
        } else {
            navView.setVisibility(View.VISIBLE);
        }
    }

    // 模拟点击导航，给外部使用
    public void clickNav(String target) {
        switch (target) {
            case "message":
                navView.setSelectedItemId(R.id.bottom_menu_message);
                break;
        }
    }


    // 设置状态栏白底黑字、导航栏栏白色
    public void setLightTheme() {
        StatusBarKit.setBgWhiteAndFontBlack(this);
        navView.setBackgroundColor(App.bottomNaviBgWhite);
        getWindow().setNavigationBarColor(App.bottomNaviBgWhite);
    }

    // 设置状态栏黑底白字、导航栏黑色
    public void setDarkTheme() {
        StatusBarKit.setBgBlackAndFontWhite(this);
        navView.setBackgroundColor(App.bottomNaviBgBlack);
        getWindow().setNavigationBarColor(App.bottomNaviBgBlack);
    }

    // 登录检查
    private boolean checkLogin() {
        if (!App.isLogin()) {
            startActivity(new Intent(this, LoginActivity.class));
            return false;
        }
        return true;
    }


    // 关闭闪屏页
    public void closeSplashPage() {
        binding.splashView.setVisibility(View.GONE);
        binding.mainActivityFragment.setVisibility(View.VISIBLE);
        navView.setVisibility(View.VISIBLE);
    }


    // 被挤下线
    public void otherPlaceLogin() {
        DialogOkFragment dialog = new DialogOkFragment().newInstance(
                getString(R.string.otherPlaceLoginTile), getString(R.string.otherPlaceLoginContent));
        dialog.setDialogClickListener(() -> App.logout(dialog.getActivity()));
        dialog.show(getSupportFragmentManager(), "otherLogin");
    }


    // 摄像头权限需用动态获取，否则别人打视频来接不了
    private void requestCameraPermissions() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(
                    this,
                    this.getString(R.string.GetCameraPermission),
                    0,
                    perms);
        }
    }

    // 监听广播
    private class Broadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            LogKit.p("[广播]", act);
            if (act.equals(BroadCastKey.checkAddMeFinish.name())) {
                ChatlistFragment chatlistFragment = (ChatlistFragment) fragments.get(FragmentKey.chatlist);
                try {
                    chatlistFragment.initView();
                } catch (Exception e){}
            } else if (act.equals(BroadCastKey.syncMessageFinish.name())) {
                ChatlistFragment chatlistFragment = (ChatlistFragment) fragments.get(FragmentKey.chatlist);
                try {
                    chatlistFragment.initData();
                    chatlistFragment.initView();
                } catch (Exception e){}
            } else if (act.equals(BroadCastKey.refreshBadge.name())) {
                refreshBadge();
            } else if (act.equals(BroadCastKey.otherPlaceLogin.name())) {
                otherPlaceLogin();
            }
        }
    }

    // 各种角标刷新
    private void refreshBadge() {
        runOnUiThread(() -> {
            // 总角标
            TextView totalBadgeView = findViewById(R.id.totalBadgeNum);
            int total = ImHelpers.getTotalBadge();
            if (total > 0) {
                totalBadgeView.setVisibility(View.VISIBLE);
                String text = String.valueOf(total);
                if (total > 99) {
                    text = "99+";
                    totalBadgeView.setTextSize(10);
                }
                totalBadgeView.setText(text);
            } else {
                totalBadgeView.setVisibility(View.INVISIBLE);
            }
            // 新的朋友
            //ChatlistFragment chatlistFragment = (ChatlistFragment) fragments.get(FragmentKey.chatlist);
            TextView addMeBadgeView = findViewById(R.id.addMeBadge);
            if (addMeBadgeView != null) {
                //addMeBadgeView = addMeBadgeView.findViewById(R.id.badgeNum);
                int num = ImHelpers.getAddmeList().size();
                if (num > 0) {
                    addMeBadgeView.setText(String.valueOf(num));
                    addMeBadgeView.setVisibility(View.VISIBLE);
                } else {
                    addMeBadgeView.setVisibility(View.INVISIBLE);
                }
            }

        });
    }


    void debugInit() {
        // 首页
        /*binding.splashFragment.setVisibility(View.GONE);
        binding.mainActivityFragment.setVisibility(View.VISIBLE);
        HomeFragment vfg = new HomeFragment();
        getSupportFragmentManager().beginTransaction().add(binding.mainActivityFragment.getId(), vfg).commit();
        navView.setVisibility(View.VISIBLE);
        StatusBarKit.setBlackBgAndWiiteFont(this);*/

        // 直播列表页
       /* binding.splashView.setVisibility(View.GONE);
        binding.mainActivityFragment.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().add(binding.mainActivityFragment.getId(), new LiveFragment()).commit();
        navView.setVisibility(View.VISIBLE);*/

        // 用户主页
        // startActivity(new Intent(this, UserPageActivity.class));

        // testkt
        //new testkt().run();

        //App.getDb();

    }
}