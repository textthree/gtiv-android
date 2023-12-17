package com.dqd2022.dto;

public class LiveListItemDto {
    public String RoomId;
    public int UserId;
    public String Title;
    public String Cover;
    public byte State;
    private String Avatar;
    public String Nickname;
    public boolean IsFollow;

    public String getAvatar() {
        // TODO 检查是否有缓存头像图片
        return Avatar;
    }
}
