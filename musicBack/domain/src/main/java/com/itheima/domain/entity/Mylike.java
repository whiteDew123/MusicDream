package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 收藏单曲表实体，对应表 mylike
 */
@Data
@TableName("mylike")
public class Mylike implements Serializable {

    /** 音乐ID */
    private Integer music;

    /** 用户ID */
    private Integer user;
}
