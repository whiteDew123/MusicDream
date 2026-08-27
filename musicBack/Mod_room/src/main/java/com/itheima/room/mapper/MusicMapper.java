package com.itheima.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.domain.entity.Music;
import org.apache.ibatis.annotations.Mapper;

/**
 * 歌曲查询 Mapper（复用 domain 的 Music 实体，跨模块查歌库 music 表）
 * <p>
 * 依据 AGENTS.md 模块隔离铁律：不 @Autowired 其它模块的 Service，
 * 各业务模块自带对共享表的 Mapper 是既有惯例（如 Mod_recognize / Mod_like）。
 */
@Mapper
public interface MusicMapper extends BaseMapper<Music> {
}
