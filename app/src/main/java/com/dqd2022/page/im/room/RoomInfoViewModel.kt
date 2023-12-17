package com.dqd2022.page.im.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoomInfoViewModel : ViewModel() {
    private var nickname: MutableLiveData<String> = MutableLiveData()
    private var avatar: MutableLiveData<String> = MutableLiveData()

    fun setNickname(nick: String) {
        nickname.value = nick
    }

    fun setNicknameOnBackendThread(nick: String) {
        nickname.postValue(nick)
    }

    fun getNickname(): MutableLiveData<String> {
        return nickname
    }

    fun setAvatar(_avatar: String) {
        avatar.value = _avatar
    }

    fun getAvatar(): LiveData<String> {
        return avatar
    }


}