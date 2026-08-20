package com.itheima.singer.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 歌手信息视图对象
 */
@Data
public class SingerVO {

    private Integer id;
    private String username;
    private String email;
    private String phone;
    private String imageUrl;
    private String about;
    private LocalDate createTime;
    private Integer songCount;
}
