package com.dqd2022.page.im;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dqd2022.R;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.page.im.contacts.FriendListFragment;
import com.dqd2022.page.im.contacts.FriendManageFragment;
import com.dqd2022.page.im.room.RoomInfoFragment;
import com.dqd2022.page.im.setting.ImSettingFragment;
import com.dqd2022.page.im.contacts.RoomListFragment;

import kit.StatusBar.StatusBarKit;

public class ImContainterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_activity);
        StatusBarKit.setBgWhiteAndFontBlack(this);
        if (savedInstanceState == null) {
            Intent i = getIntent();
            int key = i.getIntExtra("fragmentKey", 0);
            Bundle bundle = i.getBundleExtra("bundle");
            if (key == 0) {
                return;
            }
            Fragment fragment = null;
            if (key == FragmentKey.imFriendList) {
                fragment = new FriendListFragment();
            } else if (key == FragmentKey.imGroupList) {
                fragment = new RoomListFragment();
            } else if (key == FragmentKey.imSetting) {
                fragment = new ImSettingFragment(this);
            } else if (key == FragmentKey.imFriendManage) {
                fragment = new FriendManageFragment();
            } else if (key == FragmentKey.imRoomManage) {
                fragment = new RoomInfoFragment();
            }
            if (fragment != null) {
                if (bundle != null) {
                    // 在 fragment 中使用  arguments?.getInt("xxx") 直接 get 指定字段
                    fragment.setArguments(bundle);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_fragment_container, fragment)
                        .commitNow();
            }
        }

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }
}