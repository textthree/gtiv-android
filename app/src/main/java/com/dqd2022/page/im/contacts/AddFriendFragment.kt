package com.dqd2022.page.im.contacts

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.databinding.AddFriendFragmentBinding
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import kit.LogKit
import kit.StatusBar.StatusBarKit
import org.openapitools.client.models.T3imapiv1PushMidReq
import org.openapitools.client.models.T3imapiv1PushMidRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddFriendFragment(val userId: String, val nick: String, val avatar: String) : Fragment() {
    lateinit var binding: AddFriendFragmentBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddFriendFragmentBinding.inflate(inflater, container, false)
        StatusBarKit.setBgWhiteAndFontBlack(activity)
        initView()
        return binding.root
    }

    fun initView() {
        binding.header.title.setText(R.string.apply_contacts)
        binding.header.title.visibility = View.VISIBLE
        binding.input.setText(getString(R.string.helloIam) + " " + App.myNickname)
        binding.input.requestFocus()
        binding.nickname.setText(nick)
        Glide.with(App.context).load(avatar).into(binding.avatar)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? ->
            val fm = parentFragmentManager
            val transaction = fm.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out)
            fm.popBackStack()
        }
        // 发送
        binding.send.setOnClickListener {
            val req = T3imapiv1PushMidReq(MessageType.SayHello, binding.input.text.toString(), userId)
            API().Chat.imlogicPushMidPost(req).enqueue(object : Callback<T3imapiv1PushMidRes?> {
                override fun onResponse(call: Call<T3imapiv1PushMidRes?>, response: Response<T3imapiv1PushMidRes?>) {
                    AlertUtils.toast(getString(R.string.sent))
                    parentFragmentManager.popBackStack()
                }

                override fun onFailure(call: Call<T3imapiv1PushMidRes?>, t: Throwable) {}
            })
        }
    }
}