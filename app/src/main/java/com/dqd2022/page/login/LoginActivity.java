package com.dqd2022.page.login;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dqd2022.R;
import com.dqd2022.helpers.App;

import kit.StatusBar.StatusBarKit;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        StatusBarKit.setBgWhiteAndFontBlack(this);
        App.setI18n(this);
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction beginTransaction = fm.beginTransaction();
        beginTransaction.add(R.id.login_activity_fragment, loginFragment);
        beginTransaction.commit();
    }
}