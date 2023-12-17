package com.dqd2022.page.im.contacts

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dqd2022.R
import com.dqd2022.constant.ChatType
import com.dqd2022.databinding.ContactsItemBinding
import com.dqd2022.databinding.ImRoomListFragmentBinding
import com.dqd2022.dto.ContactsItemDto
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.model.ContactsModel
import com.dqd2022.page.im.room.RoomCreateOrInviteMemberFragment

class RoomListFragment : Fragment() {
    lateinit var binding: ImRoomListFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImRoomListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { activity?.finish() }
        // 数量
        val contactTable = ContactsModel()
        val roomNum = getString(R.string.totalRoom).replace("?", contactTable.roomNum.toString())
        binding.titleCount.setText(roomNum)
        // 列表
        var roomlist = contactTable.getContactsList(ChatType.Room)
        if (roomlist.isEmpty()) {
            binding.noData.text.setText(getString(R.string.noRoom))
            binding.noData.container.visibility = View.VISIBLE
        } else {
            binding.list.setLayoutManager(LinearLayoutManager(activity))
            val adapter = ListAdapter(activity, roomlist)
            binding.list.setAdapter(adapter)
        }
        // 建群
        binding.createRoom.setOnClickListener {
            App.switchFragmentWithAnim(parentFragmentManager, RoomCreateOrInviteMemberFragment(1))
        }

    }

    // 列表适配器
    class ListAdapter(private val activity: Activity?, val list: Array<ContactsItemDto>) : RecyclerView.Adapter<holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
            val view = ContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return holder(view)
        }

        override fun onBindViewHolder(holder: holder, position: Int) {
            val item: ContactsItemDto = list[position]
            if (item.avatar != "") {
                Glide.with(activity!!.getBaseContext()).load(item.avatar).centerCrop().into(holder.view.avatar)
            } else {
                val placeholder = if (item.chatType == ChatType.Private) R.drawable.default_avatar else R.drawable.default_room_avatar
                Glide.with(activity!!.getBaseContext()).load(placeholder).centerCrop().into(holder.view.avatar)
            }
            holder.view.nickname.setText(item.nickname)
            holder.view.container.setOnClickListener {
                activity.finish()
                ImHelpers.goChatting(activity, item.contactsId, item.nickname)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }
    }

    class holder(itemView: ContactsItemBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var view: ContactsItemBinding

        init {
            view = itemView
        }
    }
}