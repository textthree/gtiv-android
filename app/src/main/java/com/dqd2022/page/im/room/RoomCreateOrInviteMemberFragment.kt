package com.dqd2022.page.im.room

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
import com.dqd2022.databinding.ContactsSelectorItemBinding
import com.dqd2022.databinding.ImRoomInveteMemberFragmentBinding
import com.dqd2022.dto.EvtUpdateRoomMemberNumber
import com.dqd2022.helpers.App
import com.dqd2022.model.RoomModel
import com.dqd2022.model.ContactsModel
import com.dqd2022.model.RoomMemberModel
import kit.LogKit
import kit.StringKit
import org.greenrobot.eventbus.EventBus


// type : 1.创建群 2.邀请成员
class RoomCreateOrInviteMemberFragment(val type: Int, val roomId: Int? = 0) : Fragment() {
    lateinit var binding: ImRoomInveteMemberFragmentBinding
    lateinit var activity: Activity
    var itemModelList = ArrayList<ItemModel>()
    var inviteNickList: MutableMap<Int, String> = mutableMapOf()
    //var vm = ContactsSelectorViewModel()

    data class ItemModel(
        var nick: String = "",
        var avatar: String = "",
        var userId: Int,
        var isExists: Boolean = false,  // 本来就已经选中的
        var isSelected: Boolean = false, // 先选中的
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ImRoomInveteMemberFragmentBinding.inflate(inflater, container, false)
        activity = requireActivity()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? ->
            parentFragmentManager.popBackStack()
        }
        binding.header.title.text = getString(R.string.selectContacts)
        binding.header.title.visibility = View.VISIBLE
        binding.header.rightSaveBtn.visibility = View.VISIBLE

        // 列表
        initList()

        // 保存
        binding.header.rightSaveBtn.setOnClickListener {
            val selectedIds = mutableListOf<Int>()
            itemModelList.forEach {
                if (it.isSelected) {
                    selectedIds.add(it.userId)
                }
            }
            if (type == 1) {
                // 建群
                RoomModel().createRoom(selectedIds)
            } else {
                // 旧群邀人
                var message = getString(R.string.inviteRoomMemberNotify).replace("?", App.myNickname)
                var nicks = ""
                for ((_, v) in inviteNickList) {
                    nicks += "$v ,"
                }
                nicks = StringKit.removeRightComma(nicks)
                message = message.replace("_", nicks)
                RoomModel().inviteMember(selectedIds.joinToString(","), roomId = roomId!!, message = message)
                EventBus.getDefault().post(EvtUpdateRoomMemberNumber(roomId))
            }
            //fm.popBackStack()
            activity.finish()
        }
    }


    fun initList() {
        binding.list.setLayoutManager(LinearLayoutManager(activity))
        var friends = ContactsModel().getContactsList(ChatType.Private)
        if (friends.isNotEmpty()) {
            if (type == 1) {
                // 创建群
                friends.forEach {
                    if (it.bizId != App.myUserId)
                        itemModelList.add(ItemModel(it.nickname, it.avatar, it.bizId))
                }
            } else {
                // 邀请成员
                val existsMembers = RoomMemberModel().getAllMemberIds(roomId!!)
                friends.forEach {
                    var isExists = false
                    if (existsMembers.contains(it.bizId)) isExists = true
                    itemModelList.add(ItemModel(it.nickname, it.avatar, it.bizId, isExists, false))
                }
            }
            val adapter = ListAdapter(itemModelList)
            binding.list.setAdapter(adapter)
        } else {
            binding.noData.container.visibility = View.VISIBLE
            binding.noData.text.text = getString(R.string.createRoom)
        }
    }


    // 列表适配器
    inner class ListAdapter(val list: ArrayList<ItemModel>) : RecyclerView.Adapter<holder>() {
        val activity = this@RoomCreateOrInviteMemberFragment

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
            val view = ContactsSelectorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return holder(view)
        }

        override fun onBindViewHolder(holder: holder, position: Int) {
            val item = list[position]
            Glide.with(activity).load(item.avatar).centerCrop().into(holder.binding.avatar)
            holder.binding.nickname.setText(item.nick)
            holder.binding.radio
            if (item.isExists) {
                holder.binding.radio.visibility = View.GONE
                holder.binding.radioDisabled.visibility = View.VISIBLE
            }
            holder.bind(item);
        }


        override fun getItemCount(): Int {
            return list.size
        }
    }

    inner class holder(itemView: ContactsSelectorItemBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var binding: ContactsSelectorItemBinding

        init {
            binding = itemView
        }

        fun bind(item: ItemModel) {
            binding.radio.setOnClickListener {
                if (item.isSelected) {
                    binding.radio.isChecked = false
                    inviteNickList.remove(item.userId)
                } else {
                    inviteNickList.put(item.userId, item.nick)
                }
                item.isSelected = !item.isSelected
            }
        }

    }

}