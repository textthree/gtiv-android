package com.dqd2022.page.im.contacts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dqd2022.MainActivity
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType

import com.dqd2022.databinding.ImFriendManageFragmentBinding
import com.dqd2022.dto.EvtClearChatRecords
import com.dqd2022.dto.EvtDeleteChatItem

import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.ImSendMessageHelper
import com.dqd2022.helpers.OkOrCancelDialogFragment
import com.dqd2022.helpers.SendPrivateMessageReqDto
import com.dqd2022.model.ContactsModel
import com.dqd2022.model.UserMessageModel
import com.dqd2022.page.im.chatting.ChattingActivity
import com.dqd2022.page.userpage.UserPageActivity
import kit.LogKit
import org.greenrobot.eventbus.EventBus
import org.openapitools.client.models.T3imapiv1DeleteContactsRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendManageFragment : Fragment() {
    lateinit var binding: ImFriendManageFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ImFriendManageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // props
        val avatar = arguments?.getString("avatar")
        val nick = arguments?.getString("nick")
        val username = arguments?.getString("username")
        val userId = arguments?.getInt("userId")

        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> activity?.finish() }
        // title
        binding.header.title.text = App.context.getString(R.string.friendManage)
        // nick、username、avatar
        binding.nick.text = nick
        Glide.with(App.context).load(avatar).into(binding.avatar)
        binding.username.text = App.context.getString(R.string.userid) + " " + username

        // 查看主页
        binding.userPage.setOnClickListener {
            val intent = Intent(activity, UserPageActivity::class.java)
            intent.putExtra("nick", nick)
            intent.putExtra("avatar", avatar)
            intent.putExtra("userId", userId)
            activity?.startActivity(intent)
        }

        // 清空消息
        binding.clearHistory.setOnClickListener {
            OkOrCancelDialogFragment.newInstance(
                getString(R.string.clearChatRecords), getString(R.string.clearChatRecordsRemind)
            ) {
                if (userId != null && userId > 0) {
                    UserMessageModel().clearChatRecords(userId)
                    EventBus.getDefault().post(EvtClearChatRecords(true))
                    val chatId = ImHelpers.makeChatId(ChatType.Private, userId.toString())
                    EventBus.getDefault().post(EvtDeleteChatItem(chatId))
                }
            }.show(parentFragmentManager, null)
        }

        // 发消息
        binding.goChatting.setOnClickListener {
            activity?.finish()
            val chatId = ImHelpers.makeChatId(ChatType.Private, userId.toString())
            ImHelpers.goChatting(activity, chatId, nick)
        }

        // 删除好友
        binding.delete.setOnClickListener {
            OkOrCancelDialogFragment.newInstance(
                getString(R.string.deleteFriend), getString(R.string.deleteFriendTips)
            ) {
                val chatId = ImHelpers.makeChatId(ChatType.Private, userId.toString())
                UserMessageModel().clearChatRecords(userId!!)
                ContactsModel().deleteContacts(chatId)
                EventBus.getDefault().post(EvtDeleteChatItem(chatId))
                // 推送
                deleteFriend(userId!!)
                // 关闭导航栈
                activity?.finish()
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }.show(parentFragmentManager, null)
        }
    }

    private fun deleteFriend(userId: Int) {
        val req = SendPrivateMessageReqDto()
        req.Type = MessageType.DeleteContacts
        req.Message = "0"
        req.ToUsers = userId
        ImSendMessageHelper.sendPrivateMessage(req)
        API().Contacts.contactsDelete(userId.toString()).enqueue(object : Callback<T3imapiv1DeleteContactsRes> {
            override fun onFailure(call: Call<T3imapiv1DeleteContactsRes>, t: Throwable) {}
            override fun onResponse(call: Call<T3imapiv1DeleteContactsRes>, response: Response<T3imapiv1DeleteContactsRes>) {
                LogKit.p("删除成功")
            }
        })
    }

}