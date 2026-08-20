package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("music")
public class Music implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer musicId;

    private Integer fromSinger;

    private String musicName;

    private String musicUrl;

    private Integer activation;

    private Integer listenNumb;

    private String imageUrl;

    private Integer timelength;

    private LocalDate createTime;

    private String tags;

    private String lyric;
}
