package com.dqd2022.storage.entity;

import litepal.annotation.Column;
import litepal.crud.LitePalSupport;

public class RoomMemberEntity extends LitePalSupport {
    public Integer room_id;

    public int user_id;

    public String nickname;

    public String avatar;

    public String avatar_origin; // 原始 http 地址头像

    public String role;

    public Long create_time;

    public Long update_time;


}
