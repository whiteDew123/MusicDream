package com.itheima.msg.dto;

import lombok.Data;

@Data
public class PublishMsgRequest {

    private String title;

    private Integer userId;

    private String msg;
}