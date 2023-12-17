package com.dqd2022.page.im.chatting;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.dqd2022.constant.ChatType;
import com.dqd2022.dto.MessageDto;
import com.dqd2022.dto.RoomMemberInfoDto;
import com.dqd2022.dto.UserinfoDto;
import com.dqd2022.model.MessageModel;
import com.dqd2022.model.RoomMemberModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kit.LogKit;

public class RecycleViewListener implements RecyclerView.OnTouchListener {
    boolean hasMore = true;
    RecyclerView recyclerView;
    MessageModel messageModel;
    int page = 1;
    int rows;
    int chatType, bizId;
    ChattingActivity activity;
    private boolean viewingHistory;


    RecycleViewListener(ChattingActivity activity) {
        this.activity = activity;
        this.recyclerView = activity.recyclerView;
        this.rows = activity.rows;
        messageModel = new MessageModel();
        chatType = activity.chatType;
        bizId = activity.bizId;
    }


    // 监听 recyclerView 滑动事件
    public void initScrollListener() {
        cacheRoomMemberInfo(activity.messageList);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    // 是否滑到顶部，即下拉加载更多
                    if (!recyclerView.canScrollVertically(-1)) {
                        LogKit.p("到顶了" + activity.adapter.messageList.size());
                        if (!hasMore) {
                            return;
                        }
                        new Handler().post(() -> {
                            int length = loadHistory();
                            if (hasMore)
                                recyclerView.scrollToPosition(rows - 1);
                            else
                                recyclerView.scrollToPosition(length - 1);
                        });
                    }
                }
            }
        });
        recyclerView.setOnTouchListener(this);
    }

    // 群聊，消息列表头像下载，异步更新头像
    // if (chatType == ChatType.Room) cacheRoomMemberInfo();
    void cacheRoomMemberInfo(List<MessageDto> list) {
        if (chatType != ChatType.Room) return;
        new Thread(() -> {
            boolean cached = false;
            int size = list.size();
            Map<Integer, RoomMemberInfoDto> cachedList = new HashMap();
            for (int i = 0; i < size; i++) {
                MessageDto item = list.get(i);
                int uid = Integer.valueOf(item.fromUserId);
                if (cachedList.get(uid) != null) {
                    item.avatar = cachedList.get(uid).avatar;
                    item.nickname = cachedList.get(uid).nickname;
                    activity.adapter.messageList.set(i, item);
                    continue;
                }
                if ((item.nickname == null || item.avatar == null) && !item.senderQuited) {
                    RoomMemberModel table = new RoomMemberModel();
                    RoomMemberInfoDto userinfo = table.cacheMember(activity.bizId, uid, null);
                    if (userinfo == null) continue;
                    item.avatar = userinfo.avatar;
                    item.nickname = userinfo.nickname;
                    activity.adapter.messageList.set(i, item);
                    cachedList.put(uid, userinfo);
                    cached = true;
                }
            }
            // 重渲染
            if (cached) {
                activity.runOnUiThread(() -> {
                    activity.adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }


    // 加载下一页
    int loadHistory() {
        page++;
        LogKit.p("加载第", page, "页");
        List<MessageDto> list = messageModel.getList(chatType, bizId, page, rows);
        int messageLength = list.size();
        if (messageLength < rows) {
            hasMore = false;
        }
        for (int i = messageLength - 1; i >= 0; i--) {
            activity.adapter.messageList.add(0, list.get(i));
            activity.adapter.notifyItemInserted(0);
            LogKit.p("recyclerView 滑动事件");
        }
        return messageLength;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 如果 return true 就无法滚动了
        return false;
    }
}
