package com.dqd2022.page.im.contacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.BroadCastKey
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.FragmentKey
import com.dqd2022.databinding.ContactsItemBinding
import com.dqd2022.databinding.ImContactsFragmentBinding
import com.dqd2022.databinding.NewFriendItemBinding
import com.dqd2022.dto.ContactsItemDto
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import com.dqd2022.helpers.CacheHelpers
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.interfaces.CallbackWithStringArg
import com.dqd2022.model.ContactsModel
import com.dqd2022.page.im.chatlist.ChatlistFragment
import org.openapitools.client.models.T3imapiv1AddContactsReq
import org.openapitools.client.models.T3imapiv1AddContactsRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendListFragment : Fragment() {
    lateinit var binding: ImContactsFragmentBinding
    val contactTable = ContactsModel()
    val contactsModel = ContactsModel()
    lateinit var adapter: ListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImContactsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> activity?.finish() }
        // 添加
        binding.add.setOnClickListener { l ->
            val fm = parentFragmentManager
            val transaction = fm.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_right_in_fragment, R.anim.slide_left_out_fragment,
                R.anim.slide_left_in_fragment, R.anim.slide_right_out_fragment
            )
            transaction.replace(R.id.activity_fragment_container, SearchUserFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        // 数量
        val num = getString(R.string.totalFriends).replace("?", contactTable.friendNum.toString())
        binding.titleCount.setText(num)
        // 列表
        initList()
    }

    fun initList() {
        binding.list.layoutManager = LinearLayoutManager(activity)
        var contactsList = contactTable.getContactsList(ChatType.Private)
        if (contactsList?.size ?: 0 > 0) {
            adapter = ListAdapter(this, activity, contactsList)
            binding.list.setAdapter(adapter)
        }
    }

    // 列表适配器
    inner class ListAdapter(val that: FriendListFragment, private val activity: Activity?, val list: Array<ContactsItemDto>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val NewFriend = 0
        private val Friend = 1

        // 使用列表索引根据条件区分布局类型
        override fun getItemViewType(position: Int): Int {
            return if (ImHelpers.getAddmeList() != null && position <= ImHelpers.getAddmeList().size - 1) {
                NewFriend
            } else {
                Friend
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == NewFriend) {
                val view = NewFriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return newFriendHolder(view)
            } else {
                val view = ContactsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return friendHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is newFriendHolder -> {
                    val item = ImHelpers.getAddmeList().get(position)
                    Glide.with(App.context).load(item.Avatar).centerCrop().into(holder.view.avatar)
                    holder.view.nickname.setText(item.Nick)
                    // 接受好友
                    holder.view.accept.setOnClickListener {
                        ImHelpers.addMeListRemoveOne(position)
                        ChatlistFragment.getBinding().addMeBadge.badgeNum.setText(ImHelpers.getAddmeList().size.toString())
                        that.initList();
                        val chatId = ImHelpers.makeChatId(ChatType.Private, item.UserId.toString())
                        val avatar = CacheHelpers().downloadAvatar(item.Avatar, true)
                        that.contactTable.saveOnePrivateContacts(
                            chatId,
                            item.UserId.toString(),
                            item.Nick,
                            item.Username,
                            item.Gender.toString(),
                            avatar.localFileUri,
                            "0"
                        )
                        val req = T3imapiv1AddContactsReq(item.UserId.toString(), 1)
                        API().Contacts.contactsAddPost(req).enqueue(object : Callback<T3imapiv1AddContactsRes> {
                            override fun onResponse(call: Call<T3imapiv1AddContactsRes>, response: Response<T3imapiv1AddContactsRes>) {}
                            override fun onFailure(call: Call<T3imapiv1AddContactsRes>, t: Throwable) {}
                        })
                        AlertUtils.toast(activity?.getString(R.string.added))
                        activity?.sendBroadcast(Intent(BroadCastKey.refreshBadge.name))
                    }
                    // 拒绝加好友
                    holder.view.refuse.setOnClickListener {
                        ImHelpers.addMeListRemoveOne(position)
                        ChatlistFragment.getBinding().addMeBadge.badgeNum.setText(ImHelpers.getAddmeList().size.toString())
                        that.initList()
                        val req = T3imapiv1AddContactsReq(item.UserId.toString(), 2)
                        API().Contacts.contactsAddPost(req).enqueue(object : Callback<T3imapiv1AddContactsRes> {
                            override fun onResponse(call: Call<T3imapiv1AddContactsRes>, response: Response<T3imapiv1AddContactsRes>) {}
                            override fun onFailure(call: Call<T3imapiv1AddContactsRes>, t: Throwable) {}
                        })
                        AlertUtils.toast(activity?.getString(R.string.refused))
                        activity?.sendBroadcast(Intent(BroadCastKey.refreshBadge.name))
                    }
                }

                is friendHolder -> {
                    var listIndex = position
                    if (ImHelpers.getAddmeList() != null) {
                        listIndex = position - ImHelpers.getAddmeList().size
                    }
                    val item: ContactsItemDto = list[listIndex]
                    // 点击进入好友管理页面
                    holder.view.container.setOnClickListener { l ->
                        activity?.finish()
                        //ImHelpers.goChatting(activity, item.contactsId, item.nickname)
                        val bundle = Bundle()
                        bundle.putString("nick", item.nickname);
                        bundle.putString("username", item.username);
                        bundle.putString("avatar", item.avatar);
                        bundle.putInt("userId", item.bizId);
                        App.startImContainerActiviy(activity, FragmentKey.imFriendManage, bundle);
                    }
                    // 昵称头像
                    if (item.avatar != "") {
                        Glide.with(activity!!.getBaseContext()).load(item.avatar).centerCrop().into(holder.view.avatar)
                    } else {
                        val placeholder = if (item.chatType == ChatType.Private) R.drawable.default_avatar else R.drawable.default_room_avatar
                        Glide.with(activity!!.getBaseContext()).load(placeholder).centerCrop().into(holder.view.avatar)
                    }
                    holder.view.nickname.setText(item.nickname)
                    // 更新数据
                    if (item.nickname == "") {
                        contactsModel.updateFriendInfo(item.contactsId, item.bizId, object : CallbackWithStringArg {
                            override fun apply(arg: String) {
                                item.nickname = arg
                                adapter.notifyItemChanged(listIndex)
                            }
                        })
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            if (ImHelpers.getAddmeList() != null) {
                return list.size + ImHelpers.getAddmeList().size
            }
            return list.size
        }
    }

    class newFriendHolder(itemView: NewFriendItemBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var view: NewFriendItemBinding

        init {
            view = itemView
        }
    }

    class friendHolder(itemView: ContactsItemBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var view: ContactsItemBinding

        init {
            view = itemView
        }
    }

}