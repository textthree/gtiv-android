package com.dqd2022.page.im.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.MessageType
import com.dqd2022.constant.RoomMemberRole
import com.dqd2022.databinding.ImRoomManageMemberItemBinding
import com.dqd2022.databinding.ImRoomMemberManageFragmentBinding
import com.dqd2022.dto.RoomMemberInfoDto
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import com.dqd2022.helpers.DialogOkFragment
import com.dqd2022.helpers.ImSendMessageHelper
import com.dqd2022.model.RoomMemberModel
import kit.LogKit
import org.openapitools.client.models.T3imapiv1RemoveMemberRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomMemberManageFragment(val roomId: Int) : Fragment() {
    lateinit var binding: ImRoomMemberManageFragmentBinding
    val model = RoomMemberModel()
    val memberList = ArrayList<RoomMemberInfoDto>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImRoomMemberManageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> activity?.finish() }
        initData()
        initView()
        initList()
    }

    fun initData() {
        val members = model.localMemberList(roomId, 1, 1000)
        if (members != null) memberList.addAll(members.asList())
    }

    fun initView() {
        binding.header.title.text = getString(R.string.roomMemberManage)
    }

    private fun initList() {
        binding.list.layoutManager = LinearLayoutManager(activity)
        binding.list.adapter = ListAdapter()
        /*binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    LogKit.p("到底部了")
                }
            }
        })*/
    }


    inner class ListAdapter : RecyclerView.Adapter<holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
            val view = ImRoomManageMemberItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return holder(view)
        }

        override fun onBindViewHolder(holder: holder, position: Int) {
            val item = memberList[position]
            Glide.with(App.context).load(item.avatar).into(holder.view.avatar)
            holder.view.nickname.text = item.nickname
            // 不能移除管理员
            if (item.role > RoomMemberRole.member) holder.view.remove.visibility = View.GONE
            holder.listener(item, position)
        }

        override fun getItemCount(): Int {
            return memberList.size
        }
    }

    inner class holder(item: ImRoomManageMemberItemBinding) : RecyclerView.ViewHolder(item.getRoot()) {
        var view: ImRoomManageMemberItemBinding

        init {
            view = item
        }

        fun listener(item: RoomMemberInfoDto, position: Int) {
            view.remove.setOnClickListener {
                DialogOkFragment.newInstance(
                    getString(R.string.removeRoomMember),
                    getString(R.string.remvoeRoomMemberTips),
                    {
                        removeMember(roomId, item.userId, item.nickname)
                        // 从列表中消除
                        memberList.removeAt(position)
                        binding.list.adapter?.notifyItemRemoved(position)
                    }
                ).show(parentFragmentManager, null)
            }
        }
    }

    // 移除群聊
    fun removeMember(roomId: Int, userId: Int, nick: String) {
        // 本地删
        RoomMemberModel().delete(roomId, userId)
        // 服务端删
        API().Room.roomRemoveMemberDelete(roomId.toString(), userId.toString()).enqueue(object : Callback<T3imapiv1RemoveMemberRes> {
            override fun onResponse(call: Call<T3imapiv1RemoveMemberRes>, response: Response<T3imapiv1RemoveMemberRes>) {
                if (!response.isSuccessful) AlertUtils.toast("Network Error")
            }

            override fun onFailure(call: Call<T3imapiv1RemoveMemberRes>, t: Throwable) {
                AlertUtils.toast("Failure")
            }
        })
        // 发推送
        val content = App.context.getString(R.string.roomRemoveMember).replace("?", nick)
        val msg = JSONObject()
        msg.put("content", content)
        msg.put("userId", userId)
        ImSendMessageHelper.sendMessage(MessageType.RoomRemoveMember, msg, 0, roomId)
    }
}