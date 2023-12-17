package com.dqd2022.storage.entity;

import litepal.annotation.Column;
import litepal.crud.LitePalSupport;

public class RoomMessageEntity extends LitePalSupport {
    public String local_msgid;

    public String server_msgid;

    @Column(index = true)
    public int roomId;

    // 服务端消息时间
    @Column(index = true)
    public Long server_time;

    // 消息类型
    public int type;

    public int fromUser;

    public String message;

    // 消息发送者是否已经退群
    public int sender_quited;
}

