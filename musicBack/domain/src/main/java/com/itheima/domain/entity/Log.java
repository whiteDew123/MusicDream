package com.itheima.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("log")
public class Log implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer logId;

    @TableField("userName")
    private String userName;

    private String doSome;

    @TableField("MusicName")
    private String musicName;

    private LocalDate createDate;
}