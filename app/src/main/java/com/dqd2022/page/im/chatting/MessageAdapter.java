package com.dqd2022.page.im.chatting;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.R;
import com.dqd2022.constant.MessageType;
import com.dqd2022.dto.MessageDto;
import com.dqd2022.helpers.App;
import com.dqd2022.model.MessageModel;
import com.facebook.drawee.view.SimpleDraweeView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import kit.ImageKit;
import kit.LogKit;
import kit.StringKit;

/**
 * 本类继承自 RecyclerView.Adapter，须重写 onCreateViewHolder()、onBindViewHolder()、getItemCount() 这 3 个方法
 * RecyclerView 的每条数据都会过一遍适配器，我在这里面做每条数据的渲染处理
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ListHolder> {
    public List<MessageDto> messageList;
    private Activity activity;
    private int itemFromUserid;
    public ChattingViewModel vm;
    private MessageModel model;
    int chatType;

    // 1、首先构造函数用于把要展示的数据源传进来，并赋值给一个全局变量 messageList，
    // 后续的操作都将在这个数据源上进行，这也是 MessageAdapter 接收外来数据的唯一方法
    public MessageAdapter(Activity activity, List<MessageDto> list, int chatType) {
        messageList = list;
        this.model = new MessageModel();
        this.activity = activity;
        this.chatType = chatType;
    }

    public List<MessageDto> getList() {
        return messageList;
    }

    // 所有布局公用一个 holder
    static class ListHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView avatar;
        TextView nickname;
        TextView time;
        TextView textContent;
        SimpleDraweeView photoThumb;
        SimpleDraweeView videoThumb;
        LinearLayout videoThumbLayout;
        TextView audioDuration;
        LinearLayout messageVoice;


        // 构造函数中要传入一个 View 参数，这个参数通常就是 RecyclerView 子项的最外层布局，用于获取控件实例
        public ListHolder(View itemView) {
            super(itemView);
            avatar = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
            nickname = (TextView) itemView.findViewById(R.id.nickname);
            time = (TextView) itemView.findViewById(R.id.time);
            textContent = (TextView) itemView.findViewById(R.id.content);
            photoThumb = (SimpleDraweeView) itemView.findViewById(R.id.photo_thumb);
            videoThumb = (SimpleDraweeView) itemView.findViewById(R.id.video_thumb);
            videoThumbLayout = itemView.findViewById(R.id.videoThumbLayout);
            audioDuration = (TextView) itemView.findViewById(R.id.audio_duration);
            messageVoice = (LinearLayout) itemView.findViewById(R.id.message_audio);
        }
    }


    // 从 JavaBean 获取值设置到控件
    public void setItemToLayout(ListHolder holder, MessageDto item, int position) {
        boolean ismMine = false;
        if (itemFromUserid == App.myUserId) {
            ismMine = true;
        }
        if (holder.avatar != null) {
            String avatar = item.avatar;
            if (avatar == null || avatar.equals("") || avatar.equals("default")) {
                holder.avatar.setImageURI(ImageKit.drawable2uri(activity, R.drawable.default_avatar));
            } else {
                holder.avatar.setImageURI(avatar);
            }
        }
        if (holder.nickname != null) holder.nickname.setText(item.nickname);
        if (holder.time != null) holder.time.setText(item.getTime());
        // 消息内容
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        float scale = dm.density;
        // 目前分享视频和直接发送视频是一样的
        if (item.msgType == MessageType.VideoShare) item.msgType = MessageType.Video;
        switch (item.msgType) {
            case MessageType.Text:
                if (holder.time == null) break;
                holder.textContent.setText(item.content);
                vm.textLongPress(holder.textContent, item);
                break;
            case MessageType.Photo:
                JSONObject jsonObject = JSON.parseObject(item.content);
                String photoUri = jsonObject.getString("uri");
                int w = jsonObject.getInteger("w");
                int h = jsonObject.getInteger("h");
                int[] scaleThumb = thumbScale(w, h);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scaleThumb[0], scaleThumb[1]);
                layoutParams.topMargin = (int) scale * 6;
                holder.photoThumb.setLayoutParams(layoutParams);
                holder.photoThumb.setImageURI(photoUri);
                vm.photoLongPress(holder.photoThumb, item, getItemCount(), position);
                vm.photoPress(holder.photoThumb, item);
                if (!ismMine)
                    this.model.downloadAssets(chatType, MessageType.Photo, item.serverMsgId, photoUri, item.content);
                break;
            case MessageType.Video:
                JSONObject videoMsg = JSON.parseObject(item.content);
                LogKit.p(item.content);
                String videoUrl = videoMsg.getString("uri");
                String videoCover = videoMsg.getString("cover");
                w = videoMsg.getInteger("w");
                h = videoMsg.getInteger("h");
                scaleThumb = thumbScale(w, h);
                FrameLayout.LayoutParams videoLayoutParams = new FrameLayout.LayoutParams(scaleThumb[0], scaleThumb[1]);
                holder.videoThumb.setLayoutParams(videoLayoutParams);
                holder.videoThumb.setImageURI(videoCover);
                vm.videoLongPress(holder.videoThumb, item, getItemCount(), position);
                vm.videoPress(holder.videoThumb, videoUrl);
                // 遮罩层
                holder.videoThumbLayout.setLayoutParams(new FrameLayout.LayoutParams(scaleThumb[0], scaleThumb[1]));
                LogKit.p("视频封面:", videoCover);
                LogKit.p("视频 URL:", videoUrl);
                break;
            case MessageType.Voice:
                com.alibaba.fastjson.JSONObject obj = JSON.parseObject(item.content);
                String duration = obj.getString("duration") + "s";
                if (holder.audioDuration != null) holder.audioDuration.setText(duration);
                String voiceUrl = obj.getString("uri");
                vm.voicePress(holder.messageVoice, voiceUrl);
                vm.voiceLongPress(holder.messageVoice, item, getItemCount(), position);
                if (!ismMine)
                    this.model.downloadAssets(chatType, MessageType.Voice, item.serverMsgId, voiceUrl, item.content);
                break;
            case MessageType.Repeal:
                if (holder.textContent != null)
                    holder.textContent.setText(activity.getString(R.string.repealMsg));
                break;
            case MessageType.RoomRemoveMember:
                obj = JSON.parseObject(item.content);
                String content = obj.getString("content");
                if (holder.textContent != null) holder.textContent.setText(content);
                break;
            default:
                if (holder.textContent != null) holder.textContent.setText(item.content);
                break;
        }
        vm.avatarPress(holder.avatar, item);
    }


    // 设置 viewType , 以便 onCreateViewHolder() 时根据 viewType 加载不同布局，无论屏幕是否滚动到都会执行
    @Override
    public int getItemViewType(int position) {
        MessageDto item = messageList.get(position);

        Boolean isMine = false;
        itemFromUserid = StringKit.parseInt(item.fromUserId);
        if (itemFromUserid == App.myUserId) {
            isMine = true;
        }
        int layoutType = -1;
        if (isMine) {
            layoutType = item.msgType + 100;
        } else {
            layoutType = item.msgType;
        }
        // LogKit.p("isMine:" + item.getIsMine() + "|" + layoutType);
        return layoutType;
    }

    // 创建 ViewHolder 实例，将 xml 布局加载进来，并把加载出来的布局传入到构造函数当中
    // 这里面不能做逻辑判断，因为屏幕没滚动到不会执行
    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.message_item_system;
        // 大于 100 的是用户自己的消息，在 getItemViewType 中加了 100
        if (viewType >= 100) {
            switch (viewType) {
                case MessageType.Text + 100:
                    layout = R.layout.message_item_text_right;
                    break;
                case MessageType.Photo + 100:
                    layout = R.layout.message_item_photo_right;
                    break;
                case MessageType.Video + 100:
                    layout = R.layout.message_item_video_right;
                    break;
                case MessageType.VideoShare + 100:
                    layout = R.layout.message_item_video_right;
                    break;
                case MessageType.Voice + 100:
                    layout = R.layout.message_item_voice_right;
                    break;
                case MessageType.Repeal + 100:
                    layout = R.layout.message_item_repeal;
                    break;
            }
        } else {
            switch (viewType) {
                case MessageType.Text:
                    layout = R.layout.message_item_text_left;
                    break;
                case MessageType.Photo:
                    layout = R.layout.message_item_photo_left;
                    break;
                case MessageType.Video:
                    layout = R.layout.message_item_video_left;
                    break;
                case MessageType.VideoShare:
                    layout = R.layout.message_item_video_left;
                    break;
                case MessageType.Voice:
                    layout = R.layout.message_item_voice_left;
                    break;
                case MessageType.Repeal:
                    layout = R.layout.message_item_repeal;
                    break;
            }
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        ListHolder listHolder = new ListHolder(view);
        return listHolder;
    }


    // 对 RecyclerView 子元素填充数据，会在每条数据渲染时（被滚动到屏幕内时）执行，
    // 这里我们通过 position 参数得到当前项的数据实例，然后再将数据设置到 ListHolder 的 TextView 中
    @Override
    public void onBindViewHolder(ListHolder holder, int position) {
        MessageDto item = messageList.get(position);
        try {
            setItemToLayout(holder, item, position);
        } catch (Exception e) {
            LogKit.p("[解析消息出错]", e);
            e.printStackTrace();
        }
    }

    // 告诉 RecyclerView 一共有多少子元素，直接返回数据源的长度就可以了
    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        }
        return 0;
    }


    // 对图片进行等比缩放，最大宽度不超过屏幕一半，最大高度不超过 480
    public int[] thumbScale(int w, int h) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        double dMaxW = new BigDecimal((double) screenWidth / 2).setScale(1, RoundingMode.HALF_UP).doubleValue();
        int maxW = (int) dMaxW;
        int maxH = 480;
        int[] ret = new int[2];
        // 宽高都没有超过 320 的直接使用原始大小
        if (w < maxW && h < maxH) {
            ret[0] = w;
            ret[1] = h;
            return ret;
        }
        BigDecimal bigW = new BigDecimal(w);
        BigDecimal bigH = new BigDecimal(h);
        double rate = bigW.divide(bigH, 2, RoundingMode.HALF_UP).doubleValue();
        // 正方形
        if (rate == 1 || rate == 0) {
            ret[0] = ret[1] = maxW;
        } else if (rate > 1 && w >= maxW) {
            // 横图，最大宽度固定，等比缩放宽度
            ret[0] = maxW;
            ret[1] = (int) Math.ceil(h / (w / maxW));
        } else if (rate < 1 && h >= maxH) {
            // 竖图，最大高度固定，等比缩放宽度
            ret[1] = maxH;
            ret[0] = (int) Math.ceil(w / (h / maxH));
        }
        return ret;
    }

}