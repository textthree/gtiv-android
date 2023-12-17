package com.dqd2022.page.video;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.dqd2022.R;
import com.dqd2022.api.VideosApi;
import com.dqd2022.constant.ChatType;
import com.dqd2022.constant.MessageType;
import com.dqd2022.databinding.ContactsItemBinding;
import com.dqd2022.databinding.ShareToContactsActivityBinding;
import com.dqd2022.dto.CommonResDto;
import com.dqd2022.helpers.App;
import com.dqd2022.helpers.ImSendMessageHelper;
import com.dqd2022.helpers.SQLite;
import com.dqd2022.dto.ContactsOrm;

import java.util.ArrayList;

import kit.ImageKit;
import kit.LogKit;
import kit.StatusBar.StatusBarKit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareToContactsActivity extends AppCompatActivity {
    ArrayList<ContactsOrm> list;
    ShareToContactsActivityBinding binding;
    int videoId, width, height;
    String cover, url;
    long progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setI18n(this);
        StatusBarKit.setBgWhiteAndFontBlack(this);
        setContentView(R.layout.share_to_contacts_activity);
        binding = ShareToContactsActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initData();
        initView();
    }

    @Override
    public void onBackPressed() {
        close();
    }

    void close() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putLong("progress", progress);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    void initData() {
        // 列表
        String sql = "SELECT bizId, nickname, avatar, type FROM contactsentity " +
                " WHERE belong = " + App.myUserId + " AND isDeleted = 0 ORDER by addTime ASC";
        Cursor cursor = App.getDb().rawQuery(sql, new String[]{});
        if (cursor.getCount() == 0) {
            return;
        }
        list = new ArrayList();
        while (cursor.moveToNext()) {
            ContactsOrm item = new ContactsOrm();
            item.nickname = SQLite.getStringFromCursor(cursor, "nickname");
            item.avatar = SQLite.getStringFromCursor(cursor, "avatar");
            item.bizId = SQLite.getIntFromCursor(cursor, "bizid");
            item.type = SQLite.getIntFromCursor(cursor, "type"); // 1.私聊 2.群聊
            list.add(item);
        }
        cursor.close();
        // params
        Intent i = getIntent();
        videoId = i.getIntExtra("videoId", 0);
        width = i.getIntExtra("width", 0);
        height = i.getIntExtra("height", 0);
        cover = i.getStringExtra("cover");
        url = i.getStringExtra("uri");
        progress = i.getLongExtra("progress", 0);
    }

    void initView() {
        // 列表
        if (list.size() == 0) {
            binding.emptyData.container.setVisibility(View.VISIBLE);
            binding.emptyData.text.setText(getString(R.string.noFriend));
        } else {
            RecyclerView list = binding.list;
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(new ListAdapter());
        }
        // 返回
        binding.header.commonHeaderBack.setOnClickListener(l -> {
            finish();
        });
        // 标题
        binding.header.title.setText(getString(R.string.shareToFriend));
        binding.header.title.setVisibility(View.VISIBLE);


    }

    private class ListAdapter extends RecyclerView.Adapter<Holder> {
        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ContactsItemBinding view = ContactsItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ContactsOrm item = list.get(position);
            if (item == null) {
                return;
            }
            holder.view.nickname.setText(item.nickname);
            if (item.avatar.equals("")) {
                if (item.type == 1) {
                    holder.view.avatar.setImageURI(ImageKit.drawable2uri(getApplication(), R.drawable.default_avatar));
                } else {
                    holder.view.avatar.setImageURI(ImageKit.drawable2uri(getApplication(), R.drawable.default_room_avatar));
                }
            } else {
                Glide.with(App.context).load(item.avatar).centerCrop().into(holder.view.avatar);
            }
            // 分享出去
            holder.view.container.setOnClickListener(l -> {
                incrShareCount(videoId);
                sendMessage(item.type, item.bizId);
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class Holder extends RecyclerView.ViewHolder {
        ContactsItemBinding view;

        public Holder(ContactsItemBinding item) {
            super(item.getRoot());
            view = item;
        }
    }


    // 增加分享次数
    void incrShareCount(int videoId) {
        VideosApi.getInstance().incrShareNum(videoId).enqueue(new Callback<CommonResDto>() {
            @Override
            public void onResponse(Call<CommonResDto> call, Response<CommonResDto> response) {
            }

            @Override
            public void onFailure(Call<CommonResDto> call, Throwable t) {
            }
        });
    }

    // 推送消息 1.私聊 2.群聊
    void sendMessage(int chatType, int bizId) {
        JSONObject msg = ImSendMessageHelper.makeVideoMsgBody(cover, width, height, url);
        LogKit.p(msg);
        if (chatType == ChatType.Private) {
            ImSendMessageHelper.sendMessage(MessageType.VideoShare, msg, bizId, 0);
        } else {
            ImSendMessageHelper.sendMessage(MessageType.VideoShare, msg, 0, bizId);
        }
    }

}