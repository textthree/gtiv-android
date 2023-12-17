package com.dqd2022.page.im.chatting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dqd2022.R;
import com.dqd2022.constant.CachePath;
import com.dqd2022.constant.CallAction;
import com.dqd2022.constant.CallType;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.MessageType;

import com.dqd2022.databinding.ChattingActivityBinding;
import com.dqd2022.dto.MessageDto;
import com.dqd2022.helpers.AlertUtils;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.helpers.ImSendMessageHelper;

import com.dqd2022.model.ChatlistModel;
import com.dqd2022.page.userpage.UserPageActivity;
import com.dqd2022.page.video.SinglePlayActivity;
import com.dqd2022.page.webrtc.CallActivity;
import com.facebook.drawee.view.SimpleDraweeView;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kit.AudioMediaPlayer;
import kit.LogKit;
import kit.TimeKit;
import kit.VibrateKit;
import kit.VideoKit;
import kit.VoiceMediaRecorder;
import kit.luban.Luban;
import kit.luban.OnCompressListener;

public class ChattingViewModel extends ViewModel {
    ChattingActivity activity;
    ChattingActivityBinding binding;
    float y1, y2; // 录音遮罩层滑动手势坐标
    FrameLayout speakMask;
    boolean finishedSpeakTouch = false;
    VoiceMediaRecorder mediaRecorder;
    private Timer timer;
    private int recordingSecond = 0;
    public MessageAdapter adapter;
    public RecyclerView recyclerView;
    public int chatType, bizId, roomId;
    static AudioMediaPlayer player;
    public String copyText;
    MessageDto longPressItem;


    public void init() {
        if (chatType == ChatType.Room) roomId = bizId;
        mediaRecorder = new VoiceMediaRecorder(activity);
        speakMask = activity.findViewById(R.id.speak_mask);
        if (player == null) {
            player = new AudioMediaPlayer(activity);
        }
        onClickPicSelector();
        onClickVideoSelector();
        onClickVideoCall();
        onClickVoiceCall();
        onClickMsgActMask();
        onClickDeleteMessage();
        onClickRepealMessage();
    }

    // 发消息时往 recyclerview 追加渲染消息
    void renderNewMsg(String localMsgId, int messageType, String messageBody) {
        MessageDto item = new MessageDto();
        long time = new Date().getTime();
        item.localMsgId = localMsgId;
        item.fromUserId = Integer.toString(App.myUserId);
        item.content = messageBody;
        item.msgType = messageType;
        item.setTime(App.language, time);
        item.isMine = true;
        item.nickname = App.myNickname;
        item.avatar = App.myAvatar;
        adapter.messageList.add(item);
        int lastItemPosition = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(lastItemPosition);
        recyclerView.scrollToPosition(lastItemPosition);
    }

    // 收到消息时渲染
    public void renderReceiveMsg(MessageDto item) {
        adapter.messageList.add(item);
        int lastItemPosition = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(lastItemPosition);
        recyclerView.scrollToPosition(lastItemPosition);
    }


    // 发文本消息
    public void sendText(String content) {
        JSONObject body = new JSONObject();
        body.put("content", content);
        String localMsgId = ImSendMessageHelper.sendMessage(MessageType.Text, body, bizId, roomId);
        renderNewMsg(localMsgId, MessageType.Text, content);
    }


    // 点击消息操作条遮罩层时清除消息操作条遮罩
    public void onClickMsgActMask() {
        binding.msgActionBarMask.setOnClickListener(l -> {
            cleanMsgActMask();
        });
    }

    public void cleanMsgActMask() {
        binding.msgActionBarMask.setVisibility(View.GONE);
        binding.msgActionBar.setVisibility(View.GONE);
        binding.msgActionCopy.setVisibility(View.GONE);
        binding.msgActionRepeal.setVisibility(View.GONE);
    }


    // 录音
    @SuppressLint("ClickableViewAccessibility")
    public void voiceRecord(@NonNull TextView holdAndSpeak) {
        holdAndSpeak.setOnTouchListener((v, event) -> {
            int act = event.getAction();
            if (finishedSpeakTouch && act != MotionEvent.ACTION_UP) {
                return true;
            }
            // 开始录音
            if (act == MotionEvent.ACTION_DOWN) {
                cleanMsgActMask();
                y1 = event.getY();
                long[] pattern = {0, 50};
                VibrateKit.vibrate(activity, pattern, -1);
                if (!mediaRecorder.start()) {
                    LogKit.p("Recording failed");
                    return true;
                }
                activity.findViewById(R.id.speak_mask).setVisibility(View.VISIBLE);
                speakMask.setVisibility(View.VISIBLE);
                startTimer();
                return true;
            }
            // 手指抬起，完成录音
            if (act == MotionEvent.ACTION_UP) {
                LogKit.p("手指抬起");
                if (finishedSpeakTouch == true) {
                    finishedSpeakTouch = false;
                    return true;
                }
                speakMask.setVisibility(View.GONE);
                String file = mediaRecorder.stop();
                sendVoice(file, recordingSecond);
                stopTimer();
                return true;
            }
            // 滑动
            if (act == MotionEvent.ACTION_MOVE) {
                y2 = event.getY();
                if (y1 - y2 > 300) {
                    finishedSpeakTouch = true;
                    speakMask.setVisibility(View.GONE);
                    mediaRecorder.stop();
                    stopTimer();
                    return true;
                }
            }
            return true;
        });
    }

    // 启动录音计时器
    private void startTimer() {
        TextView time = activity.findViewById(R.id.vocie_recording_time);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                recordingSecond++;
                time.setText(Integer.toString(recordingSecond));
            }
        }, 0, 1000);
    }

    // 销毁录音计时器
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            recordingSecond = 0;
        }
    }

    // 发送语音消息
    public void sendVoice(String filepath, int duration) {
        JSONObject body = new JSONObject();
        body.put("uri", filepath);
        body.put("duration", duration);
        body.put("mime", "audio/aac");
        String localMsgId = ImSendMessageHelper.sendMessage(MessageType.Voice, body, bizId, roomId);
        renderNewMsg(localMsgId, MessageType.Voice, body.toJSONString());
    }


    // 从相册选择照片
    private void onClickPicSelector() {
        activity.findViewById(R.id.select_picture).setOnClickListener(v -> {
            PictureSelector.create(activity).
                    openGallery(SelectMimeType.ofImage()).
                    setImageEngine(GlideEngine.createGlideEngine()).
                    setLanguage(App.selectorLanguage).
                    forResult(new OnResultCallbackListener<LocalMedia>() {
                        @Override
                        public void onResult(ArrayList<LocalMedia> result) {
                            List photos = new ArrayList();
                            String targetDir = CachePath.chatPhotoDir(activity);
                            for (LocalMedia item : result) {
                                int w = item.getWidth();
                                int h = item.getHeight();
                                JSONObject msgBody = new JSONObject();
                                String absolutePath = String.valueOf(Uri.fromFile(new File(item.getRealPath())));
                                msgBody.put("uri", absolutePath);
                                msgBody.put("w", w);
                                msgBody.put("h", h);
                                msgBody.put("mime", item.getMimeType());
                                photos.add(new File(item.getRealPath()));
                                // 压缩后发
                                Luban.with(activity).load(photos).ignoreBy(100).setTargetDir(targetDir)
                                        .setCompressListener(new OnCompressListener() {
                                            @Override
                                            public void onStart() {
                                            }

                                            @Override
                                            public void onSuccess(File file) {
                                                String path = "file://" + file.getAbsolutePath();
                                                LogKit.p("图片压缩完成，压缩后文件", path);
                                                msgBody.put("uri", path); // 压缩后的图片
                                                String msgId = ImSendMessageHelper.sendMessage(MessageType.Photo, msgBody, bizId, roomId);
                                                renderNewMsg(msgId, MessageType.Photo, msgBody.toJSONString());
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                LogKit.p("[图片压缩失败]", e);
                                            }
                                        }).launch();
                            }
                        }

                        @Override
                        public void onCancel() {
                        }
                    });

        });
    }

    // 从相册选择视频
    private void onClickVideoSelector() {
        activity.findViewById(R.id.select_video).setOnClickListener(v -> {
            PictureSelector.create(activity).
                    openGallery(SelectMimeType.ofVideo()).
                    setSelectionMode(SelectModeConfig.SINGLE).
                    setLanguage(App.selectorLanguage).
                    setImageEngine(GlideEngine.createGlideEngine()).
                    forResult(new OnResultCallbackListener<LocalMedia>() {
                        @Override
                        public void onResult(ArrayList<LocalMedia> result) {
                            LocalMedia video = result.get(0);
                            int size = (int) video.getSize() / (1024 * 1024);
                            LogKit.p("视频选择结果：", video.getRealPath(), video.getMimeType(), "size:", size + "M");
                            if (size > 100) {
                                AlertUtils.toast(activity.getString(R.string.sendVideoSizeLimit));
                                binding.actionPanel.setVisibility(View.GONE);
                                return;
                            }
                            HashMap img = new VideoKit().genVideoThumb(activity, video.getRealPath(), CachePath.chatPhotoDir(activity));
                            LogKit.p("截取封面：", img);
                            JSONObject msgBody = new JSONObject();
                            String videoFile = String.valueOf(Uri.fromFile(new File(video.getRealPath())));
                            String cover = img.get("uri").toString();
                            int coverW = Integer.parseInt(img.get("w").toString());
                            int coverH = Integer.parseInt(img.get("h").toString());
                            msgBody.put("cover", cover);
                            msgBody.put("cover_mime", "image/jpg");
                            msgBody.put("w", coverW);
                            msgBody.put("h", coverH);
                            msgBody.put("uri", videoFile);
                            msgBody.put("video_mime", video.getMimeType());
                            String msgId = ImSendMessageHelper.sendMessage(MessageType.Video, msgBody, bizId, roomId);
                            renderNewMsg(msgId, MessageType.Video, msgBody.toJSONString());
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        });
    }

    // 视频通话
    private void onClickVideoCall() {
        activity.findViewById(R.id.video_call).setOnClickListener(v -> {
            ImHelpers.setCalling(true);
            Intent intent = new Intent(activity, CallActivity.class);
            intent.putExtra("callAction", CallAction.CALL);
            intent.putExtra("callType", CallType.Video);
            intent.putExtra("bizId", bizId);
            activity.startActivity(intent);
        });
    }

    // 语音通话
    private void onClickVoiceCall() {
        activity.findViewById(R.id.voice_call).setOnClickListener(v -> {
            ImHelpers.setCalling(true);
            Intent intent = new Intent(activity, CallActivity.class);
            intent.putExtra("callAction", CallAction.CALL);
            intent.putExtra("callType", CallType.Vioce);
            intent.putExtra("bizId", bizId);
            activity.startActivity(intent);
        });
    }

    // 禁言
    public void banned(String content) {
        activity.findViewById(R.id.talkbar).setVisibility(View.GONE);
        activity.findViewById(R.id.banned).setVisibility(View.VISIBLE);
        TextView textView = activity.findViewById(R.id.banned_text);
        textView.setText(content);

    }

    // 解除禁言
    public void relieveBan() {
        activity.findViewById(R.id.talkbar).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.banned).setVisibility(View.GONE);
    }

    // 删除消息
    void onClickDeleteMessage() {
        binding.msgActionDelete.setOnClickListener(l -> {
            binding.msgActionBarMask.setVisibility(View.GONE);
            binding.msgActionBar.setVisibility(View.GONE);
            removeMsgItem(longPressItem, true);
        });
    }

    // 从 recyclerView 中删除一条消息
    // needUpdateChatlist 是否需要更新会话列表，撤回消息时不用更新，因为会有一条撤回消息的提示
    void removeMsgItem(MessageDto item, boolean needUpdateChatlist) {
        List<MessageDto> list = adapter.messageList;
        for (int i = 0; i < list.size(); i++) {
            MessageDto it = list.get(i);
            if ((it.isMine && it.localMsgId != null && it.localMsgId.equals(item.localMsgId)) ||
                    (!it.isMine && it.serverMsgId != null && it.serverMsgId.equals(item.serverMsgId))) {
                boolean last = false;
                if (list.size() - 1 == i) last = true;
                final boolean isLast = last;
                adapter.messageList.remove(i);
                adapter.notifyItemRemoved(i);
                final int index = i;
                // 删库、更新会话列表，
                // 使用软删除，因为如果硬删除之后 lastMessageTime 时间会拿到上一条，导致重复同步数据
                new Thread(() -> {
                    new ChattingModel(chatType).deleteMsg(it.localMsgId, it.serverMsgId, bizId);
                    // 如果删除的是最后一条消息，还要更新会话列表
                    if (isLast && needUpdateChatlist) {
                        MessageDto prev = list.get(index - 1);
                        String chatId = ImHelpers.makeChatId(chatType, String.valueOf(bizId));
                        String msgDesc = ImHelpers.getLastMsgDesc(prev.msgType, prev.content);
                        new ChatlistModel().updateAndStorageChatlist(chatId, prev.getTimeLong(), msgDesc,
                                true, "", "");
                    }
                }).start();
                break;
            }
        }
    }

    // 撤回消息
    void onClickRepealMessage() {
        binding.msgActionRepeal.setOnClickListener(l -> {
            binding.msgActionBarMask.setVisibility(View.GONE);
            binding.msgActionBar.setVisibility(View.GONE);
            String serverMsgId = new ChattingModel(chatType).getServerMsgIdByLocalMsgId(longPressItem.localMsgId, roomId);
            if (serverMsgId.equals("")) {
                LogKit.p("无法撤回，查询不到 serverMsgId");
                AlertUtils.toast(App.context.getString(R.string.operationFrequently));
                return;
            }
            removeMsgItem(longPressItem, false);
            String localMsgId;
            if (chatType == ChatType.Private) {
                localMsgId = ImSendMessageHelper.sendText(MessageType.Repeal, serverMsgId, bizId, 0);
            } else {
                localMsgId = ImSendMessageHelper.sendText(MessageType.Repeal, serverMsgId, 0, bizId);
            }
            renderNewMsg(localMsgId, MessageType.Repeal, activity.getString(R.string.repealMsg));
        });
    }


    // 长按打开消息操作条
    private void showMsgActionBar(View v, MessageDto item) {
        int[] location = new int[2];
        v.getLocationInWindow(location);
        TableLayout layout = activity.findViewById(R.id.msg_action_bar);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = location[1] - 376;
        layout.setLayoutParams(params);
        layout.setVisibility(View.VISIBLE);
        activity.findViewById(R.id.msg_action_bar_mask).setVisibility(View.VISIBLE);
        if (item.msgType == MessageType.Text)
            activity.findViewById(R.id.msg_action_copy).setVisibility(View.VISIBLE);
        //LogKit.p(myUserid, item.getFromUserId(), item.getTimeInt(), TimeKit.now());
        // FIXME: getTimeInt() 取的是服务器时间，与本地可能对不上
        if (App.myUserId == Integer.parseInt(item.fromUserId) && (TimeKit.nowMillis() - item.getTimeLong() < 120 * 1000))
            activity.findViewById(R.id.msg_action_repeal).setVisibility(View.VISIBLE);
        longPressItem = item;
        LogKit.obj(longPressItem);
    }

    // 打开图片、视频预览的 fragment
    public void openAssetsViewFragment(int msgType, String uri) {
        AssetsViewFragment fragment = new AssetsViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        bundle.putInt("msgType", msgType);
        fragment.setArguments(bundle);
        ChattingActivity chattingActivity = (ChattingActivity) activity;
        FragmentTransaction transaction = chattingActivity.getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.chatting_activity_layout, fragment);
        transaction.commit();
    }

    // 点击头像
    public void avatarPress(SimpleDraweeView avatar, MessageDto item) {
        if (avatar != null) {
            avatar.setOnClickListener(v -> {
                Intent intent = new Intent(activity, UserPageActivity.class);
                intent.putExtra("nick", item.nickname);
                intent.putExtra("avatar", item.avatar);
                intent.putExtra("userId", Integer.valueOf(item.fromUserId));
                activity.startActivity(intent);
                //  activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            });
        }
    }

    // 文本消息长按
    public void textLongPress(TextView text, MessageDto item) {
        text.setOnLongClickListener(v -> {
            showMsgActionBar(v, item);
            copyText = item.content;
            return true;
        });
    }


    // 语音点击
    public void voicePress(LinearLayout voice, String path) {
        if (voice != null) {
            voice.setOnClickListener(v -> {
                VibrateKit.vibrate(activity, new long[]{0, 50}, -1);
                // 点击的音频增在播放就直接停止
                if (path == player.getPlayingFile()) {
                    player.stop();
                } else {
                    player.paly(path);
                }
            });
        }
    }


    // 语音长按
    public void voiceLongPress(LinearLayout voice, MessageDto item, int itemCount, int position) {
        if (voice != null) {
            voice.setOnLongClickListener(v -> {
                showMsgActionBar(v, item);
                return true;
            });
        }
    }

    // 图片消息点击
    public void photoPress(SimpleDraweeView photoThumb, MessageDto item) {
        if (photoThumb != null) {
            photoThumb.setOnClickListener(v -> {
                JSONObject jsonObject = JSON.parseObject(item.content);
                String url = jsonObject.getString("uri");
                openAssetsViewFragment(MessageType.Photo, url);
            });
        }
    }

    // 图片消息长按
    public void photoLongPress(SimpleDraweeView photoThumb, MessageDto item, int itemCount, int position) {
        if (photoThumb != null) {
            photoThumb.setOnLongClickListener(v -> {
                showMsgActionBar(v, item);
                return true;
            });
        }
    }

    // 视频点击
    public void videoPress(SimpleDraweeView videoThumb, String videoUrl) {
        if (videoThumb != null) {
            videoThumb.setOnClickListener(l -> {
                Intent intent = new Intent(activity, SinglePlayActivity.class);
                intent.putExtra("uri", videoUrl);
                intent.putExtra("releaseVideo", false);
                activity.startActivity(intent);
            });
        }
    }

    // 视频长按
    public void videoLongPress(SimpleDraweeView videoThumb, MessageDto item, int itemCount, int position) {
        if (videoThumb != null) {
            videoThumb.setOnLongClickListener(v -> {
                showMsgActionBar(v, item);
                return true;
            });
        }
    }

}
