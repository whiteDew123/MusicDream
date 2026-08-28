package com.itheima.msg.dto;

import lombok.Data;

@Data
public class PublishMsgRequest {

    private String title;

    /** 点对点时的接收用户 ID，broadcast=true 时忽略 */
    private Integer userId;

    private String msg;

    /** 是否广播给所有激活用户，默认 true */
    private Boolean broadcast;
}