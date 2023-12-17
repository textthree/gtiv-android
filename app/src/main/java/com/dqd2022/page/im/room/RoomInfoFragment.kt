package com.dqd2022.page.im.room

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dqd2022.MainActivity
import com.dqd2022.R
import com.dqd2022.api.API
import com.dqd2022.constant.CachePath
import com.dqd2022.constant.ChatType
import com.dqd2022.constant.MessageType
import com.dqd2022.constant.RoomMemberRole
import com.dqd2022.databinding.RoomInfoFragmentBinding
import com.dqd2022.databinding.RoomInfoInviteButtonBinding
import com.dqd2022.databinding.RoomInfoMemberItemBinding
import com.dqd2022.dto.ContactsItemDto
import com.dqd2022.dto.EvtClearChatRecords
import com.dqd2022.dto.EvtDeleteChatItem
import com.dqd2022.dto.RoomMemberInfoDto
import com.dqd2022.helpers.AlertUtils
import com.dqd2022.helpers.App
import com.dqd2022.helpers.ImHelpers
import com.dqd2022.helpers.ImSendMessageHelper
import com.dqd2022.helpers.OkOrCancelDialogFragment
import com.dqd2022.helpers.SendRoomMessageReqDto
import com.dqd2022.helpers.qiniu.QiniuUtils

import com.dqd2022.model.ChatlistTable
import com.dqd2022.model.ContactsModel
import com.dqd2022.model.RoomMemberModel
import com.dqd2022.model.RoomMessageTable
import com.dqd2022.page.im.chatting.GlideEngine
import com.dqd2022.page.im.contacts.RoomMemberManageFragment

import com.dqd2022.page.userpage.UserPageActivity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kit.CryptoKit
import kit.LogKit
import kit.luban.Luban
import kit.luban.OnCompressListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.openapitools.client.models.T3imapiv1QuitRoomReq
import org.openapitools.client.models.T3imapiv1QuitRoomRes
import org.openapitools.client.models.T3imapiv1RoomBannedToPostReq
import org.openapitools.client.models.T3imapiv1RoomBannedToPostRes
import org.openapitools.client.models.T3imapiv1RoomModifyAvatarReq
import org.openapitools.client.models.T3imapiv1RoomModifyAvatarRes
import org.openapitools.client.models.T3imapiv1RoomRelieveBannedToPostReq
import org.openapitools.client.models.T3imapiv1RoomRelieveBannedToPostRes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class RoomInfoFragment() : Fragment() {
    lateinit var binding: RoomInfoFragmentBinding
    var roomId: Int = 0
    var page = 1
    var rows = 15
    lateinit var memberList: ArrayList<RoomMemberInfoDto>
    var roomMemberModel = RoomMemberModel()
    var chatId: String = ""
    private var roomInfo: ContactsItemDto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RoomInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initView()
        initMemberList()
        // 是否管理员
        val info = roomMemberModel.getMemberInfo(roomId, App.myUserId)
        if (info == null) {
            Thread {
                val ret = roomMemberModel.cacheMember(roomId, App.myUserId)
                //if ((ret?.role ?: 0) > RoomMemberRole.member) initAdmin()
            }.start()

        } else {
            if (info.role > RoomMemberRole.member) initAdmin()
        }
    }

    fun initData() {
        roomId = arguments?.getInt("roomId")!!
        chatId = ImHelpers.makeChatId(ChatType.Room, roomId.toString())
        roomInfo = ContactsModel().getOne(ChatType.Room, roomId)
    }

    fun initView() {
        // 返回
        binding.header.commonHeaderBack.setOnClickListener { l: View? -> activity?.finish() }
        // title
        binding.header.title.text = getString(R.string.roomInfo)
        // 更多成员
        binding.viewMore.setOnClickListener { l: View? ->
            val fragment = MoreRoomMemberFragment()
            val args = Bundle()
            args.putInt("roomId", roomId)
            fragment.arguments = args
            App.switchFragmentWithAnim(parentFragmentManager, fragment)
        }

        // 清空聊天记录
        binding.clearRecord.setOnClickListener {
            OkOrCancelDialogFragment.newInstance(
                getString(R.string.clearChatRecords), getString(R.string.clearChatRecordsRemind)
            ) {
                RoomMessageTable().clearChatRecords(roomId)
                EventBus.getDefault().post(EvtClearChatRecords(true))
                ChatlistTable().delete(chatId)
                EventBus.getDefault().post(EvtDeleteChatItem(chatId))
            }.show(parentFragmentManager, null)
        }
        // 退群
        binding.exit.setOnClickListener {
            OkOrCancelDialogFragment.newInstance(getString(R.string.exitGroupConfirm), getString(R.string.exitGroupConfirmTips)) {
                RoomMessageTable().clearChatRecords(roomId)
                ChatlistTable().delete(chatId)
                ContactsModel().deleteContacts(chatId)
                EventBus.getDefault().post(EvtDeleteChatItem(chatId))
                // 发请求
                API().Room.roomQuitPost(T3imapiv1QuitRoomReq(roomId.toString())).enqueue(object : Callback<T3imapiv1QuitRoomRes> {
                    override fun onResponse(call: Call<T3imapiv1QuitRoomRes>, response: Response<T3imapiv1QuitRoomRes>) {
                        val req = SendRoomMessageReqDto()
                        req.Type = MessageType.ExitGroup
                        req.RoomId = roomId
                        req.Message = App.myNickname + App.context.getString(R.string.sombodyExitGroup)
                        ImSendMessageHelper.sendRoomMessage(req)
                    }

                    override fun onFailure(call: Call<T3imapiv1QuitRoomRes>, t: Throwable) {
                        AlertUtils.toast("Error: Failed to exit group")
                    }
                })
                // 关闭导航栈
                activity?.finish()
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                // 重新建立连接
                ImHelpers.connClient.reconnect()
            }.show(parentFragmentManager, null)
        }
    }

    // 权限在这里查询没有选择从聊天界面传参过来，主要考虑权限变更推送改数据库就行了，
    // 不然收到权限变更的消息推送时还得发事件去修改聊天界面的数据
    fun initAdmin() {
        val vm = ViewModelProvider(this)[RoomInfoViewModel::class.java]
        binding.admin.visibility = View.VISIBLE
        vm.setNickname(roomInfo?.nickname ?: "")
        // 成员管理
        binding.memberManage.setOnClickListener { l: View? ->
            App.switchFragmentWithAnim(parentFragmentManager, RoomMemberManageFragment(roomId))
        }
        // 群名称
        binding.groupNameText.text = vm.getNickname().value
        binding.groupName.setOnClickListener { l: View? ->
            val fragment = EditRoomNameFragment()
            val bundle = Bundle()
            bundle.putString("roomName", vm.getNickname().value)
            bundle.putString("roomId", roomId.toString())
            fragment.arguments = bundle
            App.switchFragmentWithAnim(parentFragmentManager, fragment)
        }
        // 群头像
        if (roomInfo?.avatar != null && roomInfo?.avatar != "")
            Glide.with(App.context).load(roomInfo?.avatar).centerCrop().into(binding.avatar)
        binding.groupAvatar.setOnClickListener {
            avatarSelector()
        }
        // 全体禁言
        if (roomInfo?.state == 2) {
            binding.bannedAllSwitch.isChecked = true
        }
        binding.bannedAllSwitch.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            if (checked) {
                ContactsModel().updateField(chatId, "state", "2")
                API().Room.roomBannedToPostPost(T3imapiv1RoomBannedToPostReq(roomId.toString(), "0", 5))
                    .enqueue(object : Callback<T3imapiv1RoomBannedToPostRes> {
                        override fun onResponse(call: Call<T3imapiv1RoomBannedToPostRes>, response: Response<T3imapiv1RoomBannedToPostRes>) {}
                        override fun onFailure(call: Call<T3imapiv1RoomBannedToPostRes>, t: Throwable) {}
                    })
            } else {
                ContactsModel().updateField(chatId, "state", "1")
                API().Room.roomRelieveBannedToPostPost(T3imapiv1RoomRelieveBannedToPostReq(roomId.toString(), "0"))
                    .enqueue(object : Callback<T3imapiv1RoomRelieveBannedToPostRes> {
                        override fun onResponse(call: Call<T3imapiv1RoomRelieveBannedToPostRes>, response: Response<T3imapiv1RoomRelieveBannedToPostRes>) {}
                        override fun onFailure(call: Call<T3imapiv1RoomRelieveBannedToPostRes>, t: Throwable) {}
                    })
            }
        }
        // 观察数据
//        vm.getNickname().observe(viewLifecycleOwner) { newText ->
//            binding.groupNameText.text = vm.getNickname().value
//        }
    }

    // 从相册选择照片
    private fun avatarSelector() {
        PictureSelector.create(activity).openGallery(SelectMimeType.ofImage()).setSelectionMode(SelectModeConfig.SINGLE)
            .setImageEngine(GlideEngine.createGlideEngine()).setLanguage(App.selectorLanguage).forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    val item = result[0]
                    //LogKit.p("图片选择结果：", result.get(0).getRealPath(), result.get(0).getMimeType());
                    val targetDir = CachePath.chatPhotoDir(activity)
                    Luban.with(activity).load(File(item.realPath)).ignoreBy(100).setTargetDir(targetDir)
                        .setCompressListener(object : OnCompressListener {
                            override fun onStart() {}
                            override fun onSuccess(file: File) {
                                val path = "file://" + file.absolutePath
                                // LogKit.p("图片压缩完成，压缩后文件", path);
                                Glide.with(App.context).load(path).centerCrop().into(binding.avatar)
                                ContactsModel().updateField(chatId, "avatar", path)
                                // 更新服务端
                                val suffix = "." + item.mimeType.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                                val objectkey = CryptoKit.md5Encrypt("roomavatar$roomId$suffix")
                                val T = Thread {
                                    val obsUrl = QiniuUtils.appUpload(path, item.mimeType, objectkey)
                                    LogKit.p("上传完成 obsUrl：", obsUrl, " local:", path)
                                    API().Room.roomModifyAvatarPost(T3imapiv1RoomModifyAvatarReq(roomId.toString(), path))
                                        .enqueue(object : Callback<T3imapiv1RoomModifyAvatarRes> {
                                            override fun onResponse(
                                                call: Call<T3imapiv1RoomModifyAvatarRes>,
                                                response: Response<T3imapiv1RoomModifyAvatarRes>
                                            ) {
                                                // 发推送
                                                val req = SendRoomMessageReqDto()
                                                req.Type = MessageType.ModifyRoomAvatar
                                                req.RoomId = roomId
                                                req.ToUsers = 0
                                                req.Message = obsUrl
                                                ImSendMessageHelper.sendRoomMessage(req)
                                            }

                                            override fun onFailure(call: Call<T3imapiv1RoomModifyAvatarRes>, t: Throwable) {
                                                TODO("Not yet implemented")
                                            }
                                        })
                                }
                                T.start()
                            }

                            override fun onError(e: Throwable) {
                                LogKit.p("[图片压缩失败]", e)
                            }
                        }).launch()
                }

                override fun onCancel() {}
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initMemberList() {
        val list = RoomMemberModel().localMemberList(roomId, 1, rows - 1) ?: return
        memberList = ArrayList(list.asList())
        // 占位 - 邀请成员按钮
        memberList.add(RoomMemberInfoDto())
        val adapter = ListAdapter(memberList)
        binding.list.layoutManager = GridLayoutManager(activity, 5)
        binding.list.adapter = adapter
        binding.list.isNestedScrollingEnabled = false
        if (memberList.size >= rows) binding.viewMore.visibility = View.VISIBLE
        // 不足 15 条，去服务器看看有没有新的
        if (list.size < rows - 1) {
            GlobalScope.launch {
                val more = roomMemberModel.remoteMemberList(roomId, page++, rows)
                if ((more?.size ?: 0) == 0 || more == null) return@launch
                activity?.runOnUiThread {
                    // 去除占位
                    // FIXME: 这里有个 bug，从服务器拉取成员渲染时没有去重
                    memberList.removeAt(memberList.size - 1)
                    more?.forEach {
                        if (it.isNewInsert == true && memberList.size < rows - 1) {
                            //memberList.add(list[0])
                            memberList.add(it)
                        }
                    }
                    // 重新添加占位
                    memberList.add(RoomMemberInfoDto())
                    adapter.notifyDataSetChanged()
                    if (memberList.size >= rows) binding.viewMore.visibility = View.VISIBLE
                }
            }
        }
    }

    inner class ListAdapter(val list: ArrayList<RoomMemberInfoDto>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val viewMember = 1
        val viewInvite = 2

        override fun getItemViewType(position: Int): Int {
            return if (position == memberList.size - 1) {
                viewInvite
            } else {
                viewMember
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == viewMember) {
                val view = RoomInfoMemberItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return memberHolder(view)
            } else {
                val view = RoomInfoInviteButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return inviteButon(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is inviteButon -> {
                    val item = list[position]
                    holder.listener()
                }

                is memberHolder -> {
                    val item = list[position]
                    Glide.with(App.context).load(item.avatar).into(holder.binding.avatar)
                    holder.binding.nick.text = item.nickname
                    holder.listener(item)
                }
            }

        }

        override fun getItemCount(): Int {
            return list.size
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

    inner class inviteButon(itemView: RoomInfoInviteButtonBinding) : RecyclerView.ViewHolder(itemView.getRoot()) {
        var binding: RoomInfoInviteButtonBinding

        init {
            binding = itemView
        }

        fun listener() {
            binding.icon.setOnClickListener {
                App.switchFragmentWithAnim(parentFragmentManager, RoomCreateOrInviteMemberFragment(2, roomId))
            }
        }
    }


}