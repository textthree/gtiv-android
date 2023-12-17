package com.dqd2022.storage.entity;

import litepal.annotation.Column;
import litepal.crud.LitePalSupport;

public class UserMessageEntity extends LitePalSupport {
    @Column(nullable = true)
    private String local_msgid;

    @Column(nullable = true)
    private String server_msgid;

    @Column(index = true)
    private int belong;

    // 会话编号，使用 (int)fromuser + (int)toUser
    @Column(index = true)
    private int chatNo;

    // 服务端生成的消息时间
    private Long server_time;

    // 消息类型
    private int type;

    private int fromUser;

    private int toUser;

    private String message;

    public int is_delete;

}

