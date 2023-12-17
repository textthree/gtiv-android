package com.dqd2022.page.im.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class ContactsSelectorViewModel : ViewModel() {

    // demo: 可以在网络请求回调中通过 selected.postValue(result) 更新 UI
    lateinit private var selected: MutableLiveData<MutableList<Int>>


    // 发送邀请请求
    fun postInvite() {

    }
}