package com.dqd2022.page.mine;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.dqd2022.R;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.helpers.App;

import kit.StatusBar.StatusBarKit;

public class MineManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container_activity);
        StatusBarKit.setBgWhiteAndFontBlack(this);
        App.setI18n(this);
        if (savedInstanceState == null) {
            Intent i = getIntent();
            int key = i.getIntExtra("fragmentKey", 0);
            if (key == 0) {
                return;
            }
            Fragment fragment = null;
            if (key == FragmentKey.mineManager) {
                fragment = new ManagerFragment();
            } else if (key == FragmentKey.mineMyRelease) {
                fragment = new MyReleaseFragment();
            } else if (key == FragmentKey.mineMyCollect) {
                fragment = new MyCollectFragment();
            } else if (key == FragmentKey.mineSupportMeList) {
                fragment = new MySupportListFragment(this);
            } else if (key == FragmentKey.mineFansList) {
                fragment = new MyFansListFragment(this);
            } else if (key == FragmentKey.mineFollowList) {
                fragment = new MyFollowListFragment(this);
            }
            if (fragment != null) {
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