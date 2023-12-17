package com.dqd2022.page.im.chatting;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dqd2022.R;
import com.dqd2022.api.API;
import com.dqd2022.constant.BroadCastKey;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.FragmentKey;
import com.dqd2022.constant.RoomMemberRole;
import com.dqd2022.databinding.ChattingActivityBinding;
import com.dqd2022.dto.ContactsItemDto;
import com.dqd2022.dto.EvtBanned;
import com.dqd2022.dto.EvtClearChatRecords;
import com.dqd2022.dto.EvtRoomNameChanged;
import com.dqd2022.dto.EvtUpdateRoomMemberNumber;
import com.dqd2022.dto.MessageDto;
import com.dqd2022.dto.RoomMemberInfoDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImHelpers;
import com.dqd2022.model.ContactsModel;
import com.dqd2022.model.MessageModel;
import com.dqd2022.model.RoomMemberModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.openapitools.client.models.T3imapiv1RoomInfoRes;

import java.util.ArrayList;
import java.util.List;

import kit.AppKit;
import kit.SoftKeyBoardListener;
import kit.StatusBar.StatusBarKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChattingActivity extends AppCompatActivity implements View.OnClickListener {
    ChattingActivityBinding binding;
    int bizId, chatType;
    String nickname;
    int memberNum;
    public List<MessageDto> messageList = new ArrayList<>();
    MessageAdapter adapter;
    public RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    ImageView switchToVoice, switchToKeyboard, showActionPanleBtn;
    View maskLayer;
    EditText editText;
    TextView holdAndSpeak, msgActCopy, msgActRepeal;
    Button sendMsgBtn;
    String inputedText;
    TableLayout actionPanel, msgActBar;
    static ChattingViewModel vm;
    ChattingModel chattingModel;
    ContactsItemDto contactsInfo;
    static ChattingActivity instance;
    MessageModel messageModel;
    int rows = 12;
    private BroadcastReceiver broadcastReceiver;
    RoomMemberInfoDto memberInfo;


    public static ChattingActivity getInstance() {
        return instance;
    }

    public static ChattingViewModel getVm() {
        return vm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        App.setI18n(this);
        StatusBarKit.setBgWhiteAndFontBlack(this);
        binding = ChattingActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(ChattingViewModel.class);
        vm.activity = this;
        vm.binding = binding;
        initData();
        initView();
        initViewModel();
        IntentFilter filter = new IntentFilter(BroadCastKey.chattingAppendMsg.name());
        filter.addAction(BroadCastKey.chattingRepealMsg.name());
        broadcastReceiver = new BroadcastReciver();
        registerReceiver(broadcastReceiver, filter);
        // 注册事件处理器
        EventBus.getDefault().register(this);
    }

    void initData() {
        messageModel = new MessageModel();
        Intent i = getIntent();
        bizId = i.getIntExtra("bizId", 0);
        chatType = i.getIntExtra("chatType", 0);
        nickname = i.getStringExtra("nickname");
        messageList = messageModel.getList(chatType, bizId, 1, rows);
        chattingModel = new ChattingModel(chatType);
        contactsInfo = new ContactsModel().getOne(chatType, bizId);
        if (chatType == ChatType.Room) {
            memberInfo = new RoomMemberModel().getMemberInfo(bizId, App.myUserId);
            if (contactsInfo.getMemberNum() > 0) {
                memberNum = contactsInfo.getMemberNum();
            }
            binding.chattingNickname.setText(nickname + " (" + contactsInfo.getMemberNum() + ")");
            if (memberInfo != null && memberInfo.getRole() == RoomMemberRole.INSTANCE.getMember() && contactsInfo.getState() == 2) {
                vm.banned(getString(R.string.bannedAll));
            }
        } else {
            binding.chattingNickname.setText(nickname);
            if (contactsInfo.isDeleted() == 1) vm.banned(getString(R.string.unfriended));
        }
        // 拉取服务端最新信息更新
        new Thread(() -> {
            new API().getRoom().roomInfoGet(String.valueOf(bizId)).enqueue(new Callback<T3imapiv1RoomInfoRes>() {
                @Override
                public void onResponse(Call<T3imapiv1RoomInfoRes> call, Response<T3imapiv1RoomInfoRes> response) {
                    if (response.isSuccessful()) {
                        T3imapiv1RoomInfoRes res = response.body();
                        String chatId = ImHelpers.makeChatId(ChatType.Room, String.valueOf(bizId));
                        // 以服务端人数为准
                        new ContactsModel().updateField(chatId, "membernum", String.valueOf(res.getMemberNum()));
                        int num = Integer.valueOf(res.getMemberNum().toString());
                        memberNum = num;
                        if (contactsInfo == null || (contactsInfo.getMemberNum() != num)) {
                            if (chatType == ChatType.Room && num > 0) {
                                runOnUiThread(() -> {
                                    binding.chattingNickname.setText(nickname + " (" + (num) + ")");
                                });
                            }
                        }
                        // 更新禁言状态
                        if (res.getBannedAll()) {
                            runOnUiThread(() -> {
                                vm.banned(getString(R.string.bannedAll));
                            });
                        }
                        // 更新权限

                    }
                }

                @Override
                public void onFailure(Call<T3imapiv1RoomInfoRes> call, Throwable t) {
                }
            });
        }).start();
    }

    void initView() {
        recyclerView = findViewById(R.id.chat_view_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messageList, chatType);
        adapter.vm = vm;
        new RecycleViewListener(this).initScrollListener();
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        if (chatType == ChatType.Private && bizId != App.myUserId) {
            binding.voiceCall.setVisibility(View.VISIBLE);
            binding.videoCall.setVisibility(View.VISIBLE);
        }
    }

    void initViewModel() {
        vm.recyclerView = this.recyclerView;
        vm.adapter = this.adapter;
        vm.chatType = this.chatType;
        vm.bizId = this.bizId;
        vm.init();

        findViewById(R.id.chatting_back_icon).setOnClickListener(this);
        findViewById(R.id.chatting_more_icon).setOnClickListener(this);
        maskLayer = findViewById(R.id.mask_layer);
        maskLayer.setOnClickListener(this);

        switchToVoice = findViewById(R.id.switch_to_voice);
        switchToVoice.setOnClickListener(this);
        switchToKeyboard = findViewById(R.id.switch_to_keyboard);
        switchToKeyboard.setOnClickListener(this);
        editText = findViewById(R.id.edit_text);
        holdAndSpeak = findViewById(R.id.hold_and_speak);
        editTextFocusOnChange();
        editTextContentOnChange();
        softKeyboardOnChange();
        sendMsgBtn = findViewById(R.id.send_msg);
        showActionPanleBtn = findViewById(R.id.show_action_panle);
        sendMsgBtn.setOnClickListener(this);
        showActionPanleBtn.setOnClickListener(this);
        actionPanel = findViewById(R.id.action_panel);
        msgActBar = findViewById(R.id.msg_action_bar);
        msgActCopy = findViewById(R.id.msg_action_copy);
        msgActCopy.setOnClickListener(this);
        vm.voiceRecord(holdAndSpeak);
    }

    private class BroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 根据不同标识处理
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(BroadCastKey.chattingAppendMsg.name())) {
                    String localMsgId = intent.getStringExtra("localMsgId");
                    String message = intent.getStringExtra("message");
                    int msgType = intent.getIntExtra("msgType", 0);
                    vm.renderNewMsg(localMsgId, msgType, message);
                } else if (action.equals(BroadCastKey.chattingRepealMsg.name())) {
                    String serverMsgId = intent.getStringExtra("serverMsgId");
                    MessageDto item = new ChattingModel(chatType).getItemByServerId(serverMsgId);
                    if (item != null) vm.removeMsgItem(item, false);
                }
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 左上角 back
            case R.id.chatting_back_icon:
                finish();
                break;
            // 右上角 ...
            case R.id.chatting_more_icon:
                if (chatType == ChatType.Private) {
                    Bundle bundle = new Bundle();
                    bundle.putString("nick", contactsInfo.getNickname());
                    bundle.putString("username", contactsInfo.getUsername());
                    bundle.putString("avatar", contactsInfo.getAvatar());
                    bundle.putInt("userId", bizId);
                    App.startImContainerActiviy(this, FragmentKey.imFriendManage, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("roomId", bizId);
                    App.startImContainerActiviy(this, FragmentKey.imRoomManage, bundle);
                }
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                break;
            // 发文本消息
            case R.id.send_msg:
                if (inputedText.equals("")) {
                    return;
                }
                vm.sendText(inputedText);
                editText.setText("");
                inputedText = "";
                showActionPanleBtn.setVisibility(View.VISIBLE);
                sendMsgBtn.setVisibility(View.GONE);
                break;
            // 切换到录音模式
            case R.id.switch_to_voice:
                view.setVisibility(View.GONE);
                switchToKeyboard.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
                holdAndSpeak.setVisibility(View.VISIBLE);
                holdAndSpeak.setText(R.string.hold_speak);
                actionPanel.setVisibility(View.GONE);
                AppKit.hideKeyboard(ChattingActivity.this);
                vm.cleanMsgActMask();
                break;
            // 切换到打字模式
            case R.id.switch_to_keyboard:
                view.setVisibility(View.GONE);
                switchToVoice.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                holdAndSpeak.setVisibility(View.GONE);
                vm.cleanMsgActMask();
                break;
            // 点击软键盘遮罩层
            case R.id.mask_layer:
                maskLayer.setVisibility(View.GONE);          // 收起软键盘
                AppKit.hideKeyboard(ChattingActivity.this);
                actionPanel.setVisibility(View.GONE);        // 收起操作面板
                editText.clearFocus();                       // 丢弃焦点
                break;
            // 打开操作面板
            case R.id.show_action_panle:
                AppKit.hideKeyboard(ChattingActivity.this);
                actionPanel.setVisibility(View.VISIBLE);
                maskLayer.setVisibility(View.VISIBLE);
                editText.setVisibility(View.VISIBLE);
                holdAndSpeak.setVisibility(View.GONE);
                switchToVoice.setVisibility(View.VISIBLE);
                switchToKeyboard.setVisibility(View.GONE);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                vm.cleanMsgActMask();
                break;
            // 复制消息
            case R.id.msg_action_copy:
                binding.msgActionBarMask.setVisibility(View.GONE);
                msgActBar.setVisibility(View.GONE);
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("im", vm.copyText);
                manager.setPrimaryClip(clipData);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImHelpers.connClient.checkState();
    }


    // 监听输入文本变更
    private void editTextContentOnChange() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // s: 之前已输入的内容 + 新输入的内容
                if (start == 0 && before == 0) {
                    showActionPanleBtn.setVisibility(View.GONE);
                    sendMsgBtn.setVisibility(View.VISIBLE);
                } else if (start == 0 && before == 1 && count == 0) {
                    showActionPanleBtn.setVisibility(View.VISIBLE);
                    sendMsgBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputedText = s.toString();
            }
        });
    }

    // 监听输入框焦点变更
    private void editTextFocusOnChange() {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    maskLayer.setVisibility(View.VISIBLE);
                    actionPanel.setVisibility(View.GONE);
                }
            }
        });
    }


    // 进群检查禁言
    private void checkBanned() {

    }

    private void softKeyboardOnChange() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void keyBoardHide(int height) {
                //LogKit.p("关闭 " + height);
            }
        });
    }


    @Override
    public void finish() {
        super.finish();
        ImHelpers.clearChattingTarget();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onEventBusMessage(EvtClearChatRecords event) {
        messageList.clear();
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBanned(EvtBanned event) {
        vm.banned(event.getMsg());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshRoomMemberNum(EvtUpdateRoomMemberNumber event) {
        int count = new RoomMemberModel().countRoomMember(event.getRoomId());
        binding.chattingNickname.setText(nickname + " (" + count + ")");
        memberNum = count;
    }

    @Subscribe
    public void onRoomNameChanged(EvtRoomNameChanged event) {
        nickname = event.getName();
        binding.chattingNickname.setText(nickname + " (" + memberNum + ")");
    }

}
