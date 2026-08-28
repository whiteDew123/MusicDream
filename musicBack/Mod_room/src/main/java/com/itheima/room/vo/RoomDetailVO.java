package com.itheima.room.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 房间详情 VO（在 RoomVO 基础上追加成员列表与当前歌曲信息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomDetailVO extends RoomVO {

    /** 当前歌曲名称 */
    private String currentMusicName;

    /** 当前歌曲封面 */
    private String currentMusicCover;

    /** 成员列表 */
    private List<RoomMemberVO> members;
}
