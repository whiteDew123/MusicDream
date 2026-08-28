package com.itheima.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间消息实体类，对应表 room_message
 * <p>
 * seq 为房间内递增序列号，客户端可据此去重与补齐遗漏消息。
 */
@Data
@TableName("room_message")
public class RoomMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间ID */
    private Long roomId;

    /** 发送者ID（系统消息为 null） */
    private Long userId;

    /** 类型：0-文字 1-Emoji 2-系统 */
    private Integer type;

    /** 内容 */
    private String content;

    /** 房间内消息序列号（递增） */
    private Integer seq;

    /** 发送时间 */
    private LocalDateTime createTime;
}
