package com.itheima.room.vo;

import lombok.Data;

/**
 * 房间歌单项 VO（附带歌曲展示信息）
 */
@Data
public class RoomPlaylistItemVO {

    /** 歌单项主键 */
    private Long playlistId;

    /** 歌曲ID */
    private Long musicId;

    /** 歌曲名称 */
    private String musicName;

    /** 歌曲封面 */
    private String cover;

    /** 歌曲时长（秒） */
    private Integer duration;

    /** 播放地址（前端播放用） */
    private String musicUrl;

    /** 添加者用户ID */
    private Long addedBy;

    /** 添加者昵称 */
    private String addedByName;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态：0-待播 1-播放中 2-已播 */
    private Integer status;
}
