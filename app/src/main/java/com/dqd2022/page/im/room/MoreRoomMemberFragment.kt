package com.dqd2022.page.im.room

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dqd2022.R
import com.dqd2022.databinding.ImMoreRoomMemberFragmentBinding
import com.dqd2022.databinding.RoomInfoInviteButtonBinding
import com.dqd2022.databinding.RoomInfoMemberItemBinding
import com.dqd2022.dto.RoomMemberInfoDto
import com.dqd2022.helpers.App
import com.dqd2022.model.RoomMemberModel
import com.dqd2022.page.userpage.UserPageActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MoreRoomMemberFragment : Fragment() {
    lateinit var binding: ImMoreRoomMemberFragmentBinding
    lateinit var memberList: ArrayList<RoomMemberInfoDto>
    var page = 1
    var rows = 50
    var localHasMore = true;
    var remoteHasMore = true;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImMoreRoomMemberFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> activity?.finish() }
        // title
        binding.header.title.text = getString(R.string.roomMmeber)
        initList()
    }

    private fun initList() {
        var roomId = arguments?.getInt("roomId")
        val list = RoomMemberModel().localMemberList(roomId!!, 1, rows) ?: return
        memberList = ArrayList(list.asList())
        val adapter = ListAdapter()
        binding.list.layoutManager = GridLayoutManager(activity, 5)
        binding.list.adapter = adapter
        // 不足一页，去服务器看看有没有新的
        if (list.size < rows) {
            GlobalScope.launch {
                val more = RoomMemberModel().remoteMemberList(roomId, ++page, rows)
                if ((more?.size ?: 0) == 0) return@launch
                activity?.runOnUiThread {
                    more?.forEach {
                        if (it.isNewInsert == true) {
                            memberList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
        // 滑动监听
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    var localMoreCount = 0
                    if (localHasMore) {
                        val more = RoomMemberModel().localMemberList(roomId, ++page, rows)
                        localMoreCount = more?.size ?: 0
                        more?.forEach {
                            memberList.add(it)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    // 不足一页，去服务器看看有没新的
                    if (localMoreCount < rows) {
                        localHasMore = false
                        if (!remoteHasMore) return
                        GlobalScope.launch {
                            val remoteMore = RoomMemberModel().remoteMemberList(roomId, ++page, rows)
                            if ((remoteMore?.size ?: 0) == 0) {
                                remoteHasMore = false
                                return@launch
                            }
                            activity?.runOnUiThread {
                                remoteMore?.forEach {
                                    if (it.isNewInsert == true) {
                                        memberList.add(it)
                                    }
                                }
                                adapter.notifyDataSetChanged()
                                if ((remoteMore?.size ?: 0) < rows) remoteHasMore = false
                            }
                        }
                    }
                }
            }
        })
    }

    inner class ListAdapter : RecyclerView.Adapter<memberHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): memberHolder {
            val view = RoomInfoMemberItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return memberHolder(view)
        }

        override fun getItemCount(): Int {
            return memberList.size
        }

        override fun onBindViewHolder(holder: memberHolder, position: Int) {
            val item = memberList[position]
            Glide.with(App.context).load(item.avatar).into(holder.binding.avatar)
            holder.binding.nick.text = item.nickname
            holder.listener(item)
        }
    }


    inner class memberHolder(item: RoomInfoMemberItemBinding) : RecyclerView.ViewHolder(item.getRoot()) {
        var binding: RoomInfoMemberItemBinding

        init {
            binding = item
        }

        fun listener(item: RoomMemberInfoDto) {
            binding.avatar.setOnClickListener {
                val intent = Intent(activity, UserPageActivity::class.java)
                intent.putExtra("nick", item.nickname)
                intent.putExtra("avatar", item.avatar)
                intent.putExtra("userId", Integer.valueOf(item.userId))
                activity?.startActivity(intent)
            }
        }
    }

}