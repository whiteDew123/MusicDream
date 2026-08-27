package com.itheima.room.dto;

import lombok.Data;

/**
 * 切歌投票请求体（发起 / 附议共用）
 */
@Data
public class SkipVoteDTO {

    /** 目标歌曲ID */
    private Long musicId;
}
