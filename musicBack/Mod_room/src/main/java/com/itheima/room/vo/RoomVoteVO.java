package com.itheima.room.vo;

import lombok.Data;

/**
 * 切歌投票实时状态 VO（供前端展示附议数/所需/倒计时）
 */
@Data
public class RoomVoteVO {

    /** 目标歌曲ID */
    private Long musicId;

    /** 发起者用户ID（不计入附议数） */
    private Long initiatorUserId;

    /** 去重后的投票人数（含发起者） */
    private Integer votes;

    /** 附议人数（不含发起者） */
    private Integer agreeCount;

    /** 需达到的附议人数（房间总人数 50% 向上取整） */
    private Integer required;

    /** 剩余有效秒数（30s 超时） */
    private Integer remainingSeconds;

    /** 当前是否有进行中的投票 */
    private Boolean active;
}
