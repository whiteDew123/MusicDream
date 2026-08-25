package com.itheima.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 歌曲分享记录实体
 * <p>
 * 对应表 song_share，仅用于统计分享行为，不返回业务数据。
 */
@Data
@TableName("song_share")
public class SongShare implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer musicId;

    private Integer userId;

    /** 分享渠道: link/qrcode/weibo/wechat/copy */
    private String channel;

    private Date createTime;
}
