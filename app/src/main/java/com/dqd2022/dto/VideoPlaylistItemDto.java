package com.dqd2022.dto;

import android.content.Context;

import com.dqd2022.helpers.CacheHelpers;

public class VideoPlaylistItemDto {
    int videoId;
    String videoUri;
    String cover;
    int width;
    int height;
    int userId;
    String title;
    String nickname;
    String avatar;
    boolean isFollow;
    int supportNum;
    int collectNum;
    int shareNum;

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public int getVideoId() {
        return videoId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setAvatar(Context context, String avatar) {
        CacheHelpers.CacheRetDto ret = new CacheHelpers().downloadAvatar(context, avatar, true);
        if (ret.hasCache) {
            this.avatar = ret.localFileUri;
        } else {
            this.avatar = avatar;
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
    }

    public void setCollectNum(int collectNum) {
        this.collectNum = collectNum;
    }

    public int getCollectNum() {
        return collectNum;
    }

    public void setSupportNum(int supportNum) {
        this.supportNum = supportNum;
    }

    public int getSupportNum() {
        return supportNum;
    }

    public void setShareNum(int shareNum) {
        this.shareNum = shareNum;
    }

    public int getShareNum() {
        return shareNum;
    }

}
