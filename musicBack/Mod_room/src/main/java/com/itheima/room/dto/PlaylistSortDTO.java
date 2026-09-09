package com.itheima.room.dto;

import lombok.Data;

import java.util.List;

/**
 * 歌单排序请求体
 * <p>
 * 前端把整份歌单按新顺序提交，服务端据此更新每个条目的 sort_order。
 */
@Data
public class PlaylistSortDTO {

    /** 单个排序条目 */
    @Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Item {
        /** 歌曲ID */
        private Long musicId;
        /** 期望的排序值（从 0 开始） */
        private Integer sortOrder;
    }

    /** 排序条目列表 */
    private List<Item> items;
}
