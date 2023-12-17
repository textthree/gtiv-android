package com.dqd2022.page.im.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSONObject
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.databinding.MineEditNickFragmentBinding
import com.dqd2022.dto.EvtRoomNameChanged
import com.dqd2022.dto.EvtUpdateChatlist
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.ImSendMessageHelper
import com.dqd2022.model.ContactsModel
import kit.AppKit
import kit.LogKit
import org.greenrobot.eventbus.EventBus
import org.openapitools.client.models.T3imapiv1RoomModifyNameReq
import org.openapitools.client.models.T3imapiv1RoomModifyNameRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRoomNameFragment : Fragment() {
    lateinit var binding: MineEditNickFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MineEditNickFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nick.setText(arguments?.getString("roomName"))
        binding.nick.requestFocus()
        AppKit.showKeyBoard(activity, binding.nick)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> parentFragmentManager.popBackStack() }
        // title
        binding.header.title.text = getString(R.string.setGroupName)
        // 保存
        binding.submit.setOnClickListener { l: View? ->
            binding.loading.visibility = View.VISIBLE
            val nick = binding.nick.text.toString()
            val roomId = arguments?.getString("roomId")
            val chatId = ImHelpers.makeChatId(ChatType.Room, roomId.toString())
            ContactsModel().updateField(chatId, "nickname", nick);
            val req = T3imapiv1RoomModifyNameReq(roomId!!, nick)
            API().Room.roomModifyNamePost(req).enqueue(object : Callback<T3imapiv1RoomModifyNameRes> {
                override fun onResponse(call: Call<T3imapiv1RoomModifyNameRes>, response: Response<T3imapiv1RoomModifyNameRes>) {
                    LogKit.p("成功")
                }

                override fun onFailure(call: Call<T3imapiv1RoomModifyNameRes>, t: Throwable) {
                    LogKit.p("失败", t.message)
                }
            })
            // 发推送
            EventBus.getDefault().post(EvtRoomNameChanged(nick))
            EventBus.getDefault().post(EvtUpdateChatlist(chatId, nickname = nick))
            val content = getString(R.string.modifyRoomName).replace("?", App.myNickname).replace("_", nick)
            val message = JSONObject()
            message.put("operator", App.myNickname)
            message.put("name", nick)
            message.put("content", content)
            ImSendMessageHelper.sendMessage(MessageType.ModifyRoomName, message, 0, roomId.toInt())
            // 返回
            parentFragmentManager.popBackStack()
        }
    }


}