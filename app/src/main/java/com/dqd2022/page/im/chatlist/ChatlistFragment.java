package com.dqd2022.page.im.chatlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dqd2022.R;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.databinding.ImChatlistFragmentBinding;
import com.dqd2022.databinding.ImChatlistItemBinding;
import com.dqd2022.dto.ChatItemDto;
import com.dqd2022.dto.EvtDeleteChatItem;
import com.dqd2022.dto.EvtUpdateChatlist;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.helpers.LanguageUtils;
import com.dqd2022.model.ChatlistModel;
import com.dqd2022.model.ContactsModel;
import com.dqd2022.model.ChatlistTable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import kit.LogKit;
import kit.StatusBar.StatusBarKit;

public class ChatlistFragment extends Fragment {
    static FragmentActivity activity;
    public static ImChatlistFragmentBinding binding;
    static ListAdapter adapter;
    ContactsModel contactsModel;
    static LinkedHashMap<String, ChatItemDto> chatlistMap;
    static List<String> chatIds;
    ChatlistModel chatlistModel;
    static boolean viewCreated;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogKit.p("Chatlist onCreateView");
        binding = ImChatlistFragmentBinding.inflate(inflater, container, false);
        StatusBarKit.setBgWhiteAndFontBlack(activity);
        contactsModel = new ContactsModel();
        chatlistModel = new ChatlistModel();
        EventBus.getDefault().register(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LogKit.p("Chatlist onViewCreated");
        viewCreated = true;
        initData();
        initView();
        super.onViewCreated(view, savedInstanceState);
        // 联系人
        binding.contacts.setOnClickListener(l -> {
            App.startImContainerActiviy(activity, FragmentKey.imFriendList);
        });
        // 群组
        binding.group.setOnClickListener(l -> {
            App.startImContainerActiviy(activity, FragmentKey.imGroupList);
        });
        // 设置
        binding.notice.setOnClickListener(l -> {
            App.startImContainerActiviy(activity, FragmentKey.imSetting);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LogKit.p("Chatlist onResume -> 检查连接状态...");
        ImHelpers.connClient.checkState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onEventBus(EvtDeleteChatItem event) {
        LogKit.p("Chatlist onEventBus");
        String chatId = event.getChatId();
        chatlistMap.remove(chatId);
        adapter.notifyDataSetChanged();
        sortList();
    }

    // 更新列表
    @Subscribe
    public void onUpdateChatListEvent(EvtUpdateChatlist evt) {
        if (evt.getItem() != null) {
            updateChatlistForNewMessage(evt.getContactsId(), evt.getItem());
            return;
        }
        // 更新昵称
        if (evt.getNickname() != "") {
            ChatItemDto item = chatlistMap.get(evt.getContactsId());
            LogKit.p(item, item.getName(), evt.getContactsId());
            item.setName(evt.getNickname());
            updateChatlistForNewMessage(evt.getContactsId(), item);
            return;
        }
        // 更新头像
        if (evt.getAvatar() != "") {
            ChatItemDto item = chatlistMap.get(evt.getContactsId());
            item.setAvatar(evt.getAvatar());
            updateChatlistForNewMessage(evt.getContactsId(), item);
        }

    }

    public synchronized void initData() {
        LogKit.p("Chatlist initData");
        chatlistMap = new ChatlistTable().getList();
        sortList();
        LogKit.p("ChatlistFragment 重新 initData()，会话数量：", chatlistMap.size());
    }

    private static void sortList() {
        // 建立 map key 与 position 映射，数据库查出来是时间升序，这里通过 key 映射转换为倒序
        chatIds = new ArrayList<>(chatlistMap.keySet());
        Collections.reverse(chatIds);
    }

    // 从 SQLITE 获取会话列表渲染
    public synchronized void initView() {
        LogKit.p("Chatlist initView");
        if (!viewCreated) return;
        activity.runOnUiThread(() -> {
            binding.list.setLayoutManager(new LinearLayoutManager(activity));
            adapter = new ListAdapter();
            binding.list.setAdapter(adapter);
            int num = ImHelpers.getAddmeList().size();
            if (num > 0) {
                binding.addMeBadge.badgeNum.setText(String.valueOf(num));
                binding.addMeBadge.badgeNum.setVisibility(View.VISIBLE);
            }
        });
    }


    private class ListAdapter extends RecyclerView.Adapter<holder> {

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImChatlistItemBinding binding = ImChatlistItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new holder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull holder holder, int position) {
            ChatItemDto item = chatlistMap.get(chatIds.get(position));
            ImChatlistItemBinding view = holder.binding;
            // avatar
            int placholderAvatar = R.drawable.default_avatar;
            //LogKit.obj(item);
            if ((item.getAvatar().equals("") || item.getAvatar() == null) && item.getChatType() == ChatType.Room) {
                placholderAvatar = R.drawable.default_room_avatar;
            }
            Glide.with(activity).load(item.getAvatar()).placeholder(placholderAvatar).centerCrop().into(view.avatar);
            view.nick.setText(item.getName());
            view.time.setText(LanguageUtils.Stamp2ago(item.getLastMessageTime()));
            view.desc.setText(item.getLastMessageDesc());
            if (item.getBadgeNum() > 0) {
                view.badge.badgeNum.setText(String.valueOf(item.getBadgeNum()));
                view.badge.badgeNum.setVisibility(View.VISIBLE);
            }
            holder.bind(item, position);
        }

        @Override
        public int getItemCount() {
            return chatlistMap.size();
        }
    }

    private class holder extends RecyclerView.ViewHolder {
        ImChatlistItemBinding binding;

        public holder(@NonNull ImChatlistItemBinding item) {
            super(item.getRoot());
            binding = item;
        }

        void bind(ChatItemDto item, int position) {
            // 删除会话
            binding.delete.setOnClickListener(l -> {
                new ChatlistTable().delete(item.getContactsId());
                chatlistMap.remove(item.getContactsId());
                sortList();
                adapter.notifyItemRemoved(position);
            });
            // 打开会话
            binding.container.setOnClickListener(l -> {
                ImHelpers.goChatting(activity, item.getContactsId(), item.getName());
                new Thread(() -> {
                    activity.runOnUiThread(() -> {
                        item.setBadgeNum(0);
                        chatlistMap.put(chatIds.get(position), item);
                        adapter.notifyItemChanged(position);
                    });
                    new ChatlistTable().clearBadgeNum(item.getContactsId());
                    ImHelpers.totalBadgeReduce(item.getBadgeNum());
                    int chatListBadgeTotal = 0;
                    Set<String> keySet = chatlistMap.keySet();
                    Iterator<String> iterator = keySet.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        ChatItemDto value = chatlistMap.get(key);
                        chatListBadgeTotal += value.getBadgeNum();
                    }
                    if (chatListBadgeTotal == 0) ImHelpers.clearAllChatBadgeNum();
                    activity.sendBroadcast(new Intent(BroadCastKey.refreshBadge.name()));
                }).start();
            });
        }
    }

    public static ImChatlistFragmentBinding getBinding() {
        return binding;
    }

    public static FragmentActivity getCtx() {
        return activity;
    }

    public synchronized static LinkedHashMap<String, ChatItemDto> getChatlist() {
        return chatlistMap;
    }

    public synchronized static int getPositionByChatId(String chatId) {
        return chatIds.indexOf(chatId);
    }

    // 新消息
    private synchronized void updateChatlistForNewMessage(String contactsId, ChatItemDto newItem) {
        if (!viewCreated) {
            return;
        }
        activity.runOnUiThread(() -> {
            ChatItemDto oldItem = chatlistMap.get(contactsId);
            // 新会话
            if (oldItem == null) {
                chatlistMap.put(contactsId, newItem);
                sortList();
                adapter.notifyItemInserted(0);
                return;
            }
            ChatItemDto item = chatlistMap.get(contactsId);
            item.setLastMessageTime(newItem.getLastMessageTime());
            item.setLastMessageDesc(newItem.getLastMessageDesc());
            item.setBadgeNum(item.getBadgeNum() + newItem.getBadgeNum());
            item.setName(newItem.getName());
            if (newItem.getAvatar() != "") item.setAvatar(newItem.getAvatar());
            int position = getPositionByChatId(contactsId);
            // 目标会话刚好在头部
            if (position == 0) {
                chatlistMap.put(contactsId, item);
                adapter.notifyItemChanged(0);
                return;
            }
            // 目标会话不在头部
            chatlistMap.remove(contactsId);
            chatlistMap.put(contactsId, item);
            sortList();
            adapter.notifyItemMoved(position, 0);
            adapter.notifyItemChanged(0);
        });
    }


}