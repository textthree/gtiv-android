package com.dqd2022.page.mine

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
import com.dqd2022.api.UsersApi
import com.dqd2022.databinding.MineFollowListBinding
import com.dqd2022.databinding.MineUserListItemBinding
import com.dqd2022.dto.MyUserListItem
import com.dqd2022.dto.MyUserListRes
import com.dqd2022.helpers.App
import com.dqd2022.helpers.LanguageUtils
import com.dqd2022.page.userpage.UserPageActivity
import kit.LogKit


class MyFollowListFragment(private val activity: Activity) : Fragment() {
    lateinit var binding: MineFollowListBinding
    var page = 1
    var hasMore = true
    var userList = arrayOf<MyUserListItem>()
    var adapter: ListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MineFollowListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l -> activity.finish() }
        initData()
    }

    fun initData() {
        // 列表
        UsersApi.getInstance().myFollowList(page) { res: MyUserListRes ->
            userList = res.List
            initView()
        }
        // 数量
        val count = getString(R.string.totalFollowNum).replace("?", App.followNum.toString())
        binding.titleCount.setText(count)
        binding.titleCount.text = activity.getString(R.string.totalFollowNum).replace("?", App.followNum.toString())
    }

    fun initView() {
        binding.list.setLayoutManager(LinearLayoutManager(activity))
        adapter = ListAdapter(activity, userList)
        binding.list.setAdapter(adapter)
        // 监听滑动事件
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    LogKit.p("到底部了")
                    loadMore()
                }
            }
        })
    }

    // 加载更多
    fun loadMore() {
        if (!hasMore) {
            return
        }
        UsersApi.getInstance().myFollowList(++page) { res: MyUserListRes ->
            val count = res.List.size
            if (count > 0) {
                var itemPosition = userList.size
                for (i in 0 until count) {
                    userList[itemPosition] = res.List[i]
                    adapter!!.notifyItemInserted(itemPosition)
                    itemPosition++
                }
            } else {
                hasMore = false
            }
        }
    }

    // 列表适配器
    class ListAdapter(private val activity: Activity, val userList: Array<MyUserListItem>) : RecyclerView.Adapter<holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): holder {
            val view = MineUserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return holder(view)
        }

        override fun onBindViewHolder(holder: holder, position: Int) {
            val item: MyUserListItem = userList[position]
            if (item.Avatar != "") {
                Glide.with(activity.getBaseContext())
                    .load(item.Avatar)
                    .centerCrop()
                    .into(holder.view.avatar)
            }
            holder.view.nickname.setText(item.Nickname)
            holder.view.time.setText(LanguageUtils.Stamp2ago(item.CreateTime * 1000))
            holder.view.container.setOnClickListener { l ->
                val intent = Intent(activity, UserPageActivity::class.java)
                intent.putExtra("nick", item.Nickname)
                intent.putExtra("avatar", item.Avatar)
                intent.putExtra("userId", item.Id)
                activity.startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return userList.size
        }
    }

    class holder(itemView: MineUserListItemBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var view: MineUserListItemBinding

        init {
            view = itemView
        }
    }
}
