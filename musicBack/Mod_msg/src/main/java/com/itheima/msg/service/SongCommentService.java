package com.itheima.msg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.msg.entity.SongComment;

import java.util.List;

/**
 * 歌曲评论 Service 接口
 */
public interface SongCommentService extends IService<SongComment> {

    /**
     * 分页查询某首歌的一级评论列表
     *
     * @param musicId 歌曲ID
     * @param pn      页码
     * @param size    每页条数
     * @return 评论分页
     */
    Page<SongComment> listComments(Integer musicId, Integer pn, Integer size);

    /**
     * 查询某条评论的楼中楼回复
     */
    List<SongComment> listReplies(Long parentId);

    /**
     * 发表评论
     *
     * @param musicId  歌曲ID
     * @param userId   评论用户ID
     * @param content  评论内容
     * @param parentId 父评论ID（一级评论传 null）
     * @param toUserId 回复目标用户ID（楼中楼回复时传）
     * @return 新评论（含联表用户昵称/头像）
     */
    SongComment publish(Integer musicId, Integer userId, String content,
                        Long parentId, Integer toUserId);

    /**
     * 删除评论（仅评论作者或管理员可删）
     *
     * @param commentId 评论ID
     * @param userId     当前用户ID
     * @param role       当前用户角色（0=管理员）
     * @return true=删除成功，false=无权限
     */
    boolean delete(Long commentId, Integer userId, Integer role);

    /**
     * 评论点赞（+1）
     */
    boolean like(Long commentId);

    /**
     * 统计某首歌的评论数（从 music.comment_count 冗余列读取）
     */
    int countByMusicId(Integer musicId);
}
