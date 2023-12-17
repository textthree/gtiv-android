package com.dqd2022.page.im.contacts

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.databinding.ImSearchListFragmentBinding
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import kit.AppKit
import kit.LogKit
import org.openapitools.client.models.T3imapiv1SearchUserReq
import org.openapitools.client.models.T3imapiv1SearchUserRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserFragment : Fragment() {
    lateinit var binding: ImSearchListFragmentBinding
    var activity: Activity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImSearchListFragmentBinding.inflate(inflater, container, false)
        activity = getActivity()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding!!.header.commonHeaderBack.setOnClickListener { l: View? ->
            val fm = parentFragmentManager
            val transaction = fm.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out)
            fm.popBackStack()
        }
        binding.username.requestFocus()
        // 标题
        binding.header.title.text = getString(R.string.search_user)
        binding.header.title.visibility = View.VISIBLE
        // 查找
        binding.submit.setOnClickListener { l: View? ->
            AppKit.hideKeyboard(activity)
            var req = T3imapiv1SearchUserReq(binding.username.text.toString())
            API().User.userSearchPost(req).enqueue(object : Callback<T3imapiv1SearchUserRes> {
                override fun onResponse(call: Call<T3imapiv1SearchUserRes>, response: Response<T3imapiv1SearchUserRes>) {
                    if (!response.isSuccessful) return
                    val res = response.body()
                    if (res?.apiCode!!.toInt() > 0) {
                        AlertUtils.toast(getString(R.string.userNotFound))
                    } else {
                        val fragment = AddFriendFragment(res.userId!!, res.nickname!!, res.avatar!!)
                        parentFragmentManager.popBackStack()
                        App.switchFragmentWithAnim(parentFragmentManager, fragment)
                    }
                }

                override fun onFailure(call: Call<T3imapiv1SearchUserRes>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}