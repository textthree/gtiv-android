package com.dqd2022.storage.entity;

import litepal.annotation.Column;
import litepal.crud.LitePalSupport;

public class ContactsEntity extends LitePalSupport {

    // 通过下换线拼接自己和对方的 id
    @Column(unique = true)
    private String chatId;

    // 本地记录创建时间
    @Column(index = true)
    private Long addTime;

    // 用户 id 或群 id
    private int bizId;

    // 区分本地多用户切换
    private int belong;

    // 1.私聊 2.群聊
    @Column(index = true)
    private int type;

    private String nickname;
    private String username;
    private String avatar;

    // 群人数
    private int memberNum;

    // 状态: 1.正常 2.群里全体禁言
    public int state;

    private int gender;

    // 对方是否将我删除 1.是 0.否
    private int isDeleted;

    // 上次同步消息的时间，主要是给群类型的消息用
    // 记录同步时间，因为群消息在服务器上是公共的，同步后并不会删除服务器上保存的消息，
    // 本地如果使用最后一条消息的时间来做同步会有问题，当用户删除最后一条消息后下次同步时就会把已删除的消息同步出来
    // 所以把消息同步的时间保存在联系人表
    private Long lastSyncMsgTime;


    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    public int getBizId() {
        return bizId;
    }

    public void setBizId(int bizId) {
        this.bizId = bizId;
    }

    public int getBelong() {
        return belong;
    }

    public void setBelong(int belong) {
        this.belong = belong;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getLastSyncMsgTime() {
        return lastSyncMsgTime;
    }

    public void setLastSyncMsgTime(Long lastSyncMsgTime) {
        this.lastSyncMsgTime = lastSyncMsgTime;
    }
}

