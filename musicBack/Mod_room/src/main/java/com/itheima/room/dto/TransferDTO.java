package com.itheima.room.dto;

import lombok.Data;

/**
 * 房主转让请求体
 */
@Data
public class TransferDTO {

    /** 受让用户ID（须为房间成员） */
    private Long userId;
}
