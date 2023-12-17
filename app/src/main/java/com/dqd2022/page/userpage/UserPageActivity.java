package com.dqd2022.page.userpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dqd2022.R;
import com.dqd2022.helpers.App;
import com.dqd2022.page.im.contacts.AddFriendFragment;

public class UserPageActivity extends AppCompatActivity {
    long homeProgress; // 主页在播的视频进度
    volatile boolean needSwitchToMessageList;
    UserPageFragment userPageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        setContentView(R.layout.user_home_activity);
        Intent i = getIntent();
        homeProgress = i.getLongExtra("progress", 0);
        String nick = i.getStringExtra("nick");
        String avatar = i.getStringExtra("avatar");
        int userId = i.getIntExtra("userId", 0);
        userPageFragment = new UserPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        bundle.putString("nick", nick);
        bundle.putString("avatar", avatar);
        userPageFragment.setArguments(bundle); // 设置参数
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction beginTransaction = fm.beginTransaction();
        beginTransaction.add(R.id.user_home, userPageFragment);
        beginTransaction.commit();
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("progress", homeProgress);
        if (needSwitchToMessageList) {
            intent.putExtra("message", "switchToMessageList");
        }
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    void gotoAddFriend(String userId, String nickname, String avatar) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        AddFriendFragment addFriendFragment = new AddFriendFragment(userId, nickname, avatar);
        transaction.hide(userPageFragment);
        transaction.add(R.id.user_home, addFriendFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}