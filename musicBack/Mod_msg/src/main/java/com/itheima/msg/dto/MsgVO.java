package com.itheima.msg.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MsgVO {

    private Integer id;

    private String title;

    private String msg;

    private LocalDate createTime;

    private Integer isread;
}