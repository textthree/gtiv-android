package com.dqd2022.page.userpage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dqd2022.helpers.App;

import kit.NumberKit;

public class UserPageVM extends ViewModel {

    private final MutableLiveData<String> nickname;
    private final MutableLiveData<String> avatar;
    private final MutableLiveData<String> fansNum;
    public boolean isFollow, isFriend;
    int fansNumber;

    public UserPageVM() {
        this.fansNum = new MutableLiveData<>();
        this.avatar = new MutableLiveData<>();
        nickname = new MutableLiveData<>();
    }

    public void setNickname(String nick) {
        nickname.setValue(nick);
    }

    public LiveData<String> getNickname() {
        return nickname;
    }

    public void setAvatar(String _avatar) {
        avatar.setValue(_avatar);
    }

    public LiveData<String> getAvatar() {
        return avatar;
    }

    // 粉丝数量
    public void setFansNum(Integer num) {
        fansNumber = num;
        fansNum.setValue(NumberKit.formatWithUnit(App.language, num));
    }


    // fans++
    public void increaseFans() {
        setFansNum(++fansNumber);
    }

    // fans--
    public void reduceFans() {
        setFansNum(--fansNumber);
    }

    public LiveData<String> getFansNum() {
        return fansNum;
    }
}