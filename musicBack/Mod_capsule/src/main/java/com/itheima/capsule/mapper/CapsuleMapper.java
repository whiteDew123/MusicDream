package com.itheima.capsule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.capsule.vo.CapsuleVO;
import com.itheima.domain.entity.Capsule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 胶囊 Mapper
 */
public interface CapsuleMapper extends BaseMapper<Capsule> {

    /**
     * 查询我创建的胶囊列表（联表 music + user）
     */
    @Select("SELECT c.*, " +
            "m.music_name, m.music_url, m.image_url, m.timelength, " +
            "u.username AS singer_name, " +
            "sender.username AS sender_name, " +
            "IFNULL(cl.like_count, 0) AS like_count, " +
            "IF(ml.id IS NOT NULL, 1, 0) AS liked " +
            "FROM capsule c " +
            "LEFT JOIN music m ON c.music_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "LEFT JOIN user sender ON c.sender_id = sender.id " +
            "LEFT JOIN (SELECT capsule_id, COUNT(*) AS like_count FROM capsule_like GROUP BY capsule_id) cl ON cl.capsule_id = c.id " +
            "LEFT JOIN capsule_like ml ON ml.capsule_id = c.id AND ml.user_id = #{userId} " +
            "WHERE c.sender_id = #{userId} " +
            "ORDER BY c.create_time DESC")
    List<CapsuleVO> selectMyCapsules(@Param("userId") Integer userId);

    /**
     * 查询写给我的胶囊列表
     */
    @Select("SELECT c.*, " +
            "m.music_name, m.music_url, m.image_url, m.timelength, " +
            "u.username AS singer_name, " +
            "sender.username AS sender_name, " +
            "IFNULL(cl.like_count, 0) AS like_count, " +
            "IF(ml.id IS NOT NULL, 1, 0) AS liked " +
            "FROM capsule c " +
            "LEFT JOIN music m ON c.music_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "LEFT JOIN user sender ON c.sender_id = sender.id " +
            "LEFT JOIN (SELECT capsule_id, COUNT(*) AS like_count FROM capsule_like GROUP BY capsule_id) cl ON cl.capsule_id = c.id " +
            "LEFT JOIN capsule_like ml ON ml.capsule_id = c.id AND ml.user_id = #{userId} " +
            "WHERE c.receiver_id = #{userId} " +
            "ORDER BY c.create_time DESC")
    List<CapsuleVO> selectReceivedCapsules(@Param("userId") Integer userId);

    /**
     * 查询胶囊详情（含歌曲信息和点赞状态）
     */
    @Select("SELECT c.*, " +
            "m.music_name, m.music_url, m.image_url, m.timelength, " +
            "u.username AS singer_name, " +
            "sender.username AS sender_name, " +
            "IFNULL(cl.like_count, 0) AS like_count, " +
            "IF(ml.id IS NOT NULL, 1, 0) AS liked " +
            "FROM capsule c " +
            "LEFT JOIN music m ON c.music_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "LEFT JOIN user sender ON c.sender_id = sender.id " +
            "LEFT JOIN (SELECT capsule_id, COUNT(*) AS like_count FROM capsule_like GROUP BY capsule_id) cl ON cl.capsule_id = c.id " +
            "LEFT JOIN capsule_like ml ON ml.capsule_id = c.id AND ml.user_id = #{userId} " +
            "WHERE c.id = #{id}")
    CapsuleVO selectCapsuleDetail(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 查询时空广场（已公开且已解锁的胶囊）
     */
    @Select("SELECT c.*, " +
            "m.music_name, m.music_url, m.image_url, m.timelength, " +
            "u.username AS singer_name, " +
            "sender.username AS sender_name, " +
            "IFNULL(cl.like_count, 0) AS like_count, " +
            "IF(ml.id IS NOT NULL, 1, 0) AS liked " +
            "FROM capsule c " +
            "LEFT JOIN music m ON c.music_id = m.music_id " +
            "LEFT JOIN user u ON m.from_singer = u.id " +
            "LEFT JOIN user sender ON c.sender_id = sender.id " +
            "LEFT JOIN (SELECT capsule_id, COUNT(*) AS like_count FROM capsule_like GROUP BY capsule_id) cl ON cl.capsule_id = c.id " +
            "LEFT JOIN capsule_like ml ON ml.capsule_id = c.id AND ml.user_id = #{userId} " +
            "WHERE c.is_public = 1 AND c.status >= 1 " +
            "ORDER BY c.unlock_time DESC " +
            "LIMIT #{size}")
    List<CapsuleVO> selectPlazaList(@Param("userId") Integer userId, @Param("size") int size);

    /**
     * 批量解锁到期的封印胶囊
     */
    @Update("UPDATE capsule SET status = 1 WHERE status = 0 AND unlock_time <= NOW()")
    int unlockExpiredCapsules();
}
