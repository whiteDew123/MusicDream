package com.itheima.achievement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.achievement.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户成就进度 Mapper
 */
@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    /**
     * 查询用户收藏歌曲数（跨模块读 mylike 表，只读）
     * <p>
     * 注：Mod_like 实际使用的表为 mylike，字段 user/music（见 init.sql），
     * 与 Mod_like/schema.sql 中 like_music 表名不同，这里以运行库为准。
     */
    @Select("SELECT COUNT(*) FROM mylike WHERE user = #{userId}")
    int countUserLikes(@Param("userId") Integer userId);
}
