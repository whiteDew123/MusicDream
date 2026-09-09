package com.itheima.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.room.dto.RoomCreateDTO;
import com.itheima.room.dto.RoomUpdateDTO;
import com.itheima.room.entity.Room;
import com.itheima.room.vo.RoomDetailVO;
import com.itheima.room.vo.RoomVO;

import java.util.List;

/**
 * 房间 Service 接口
 */
public interface RoomService extends IService<Room> {

    /** 创建房间（创建者自动成为房主并加入房间） */
    RoomDetailVO create(RoomCreateDTO dto, Long userId);

    /** 查询房间详情（含成员列表与当前歌曲信息） */
    RoomDetailVO getDetail(Long id, Long userId);

    /** 查询房间列表（公开房间 + 我加入的房间） */
    List<RoomVO> listRooms(Long userId);

    /** 修改房间（仅房主） */
    RoomDetailVO update(RoomUpdateDTO dto, Long userId);

    /** 关闭房间（仅房主）：设置 status=2 并清空成员 */
    void close(Long id, Long userId);

    /** 加入房间：自动退出其它房间 + 校验上限与重复加入 */
    RoomDetailVO join(Long id, Long userId);

    /** 离开房间：房主离开则关闭房间，最后一人离开也关闭 */
    void leave(Long id, Long userId);

    /** 转让房主（仅房主，乐观锁防并发） */
    void transfer(Long id, Long userId, Long targetUserId);

    /** 移出成员（仅房主） */
    void kick(Long id, Long userId, Long targetUserId);

    /** 通过邀请码查询房间（公开接口，供邀请落地页展示） */
    RoomDetailVO getByInviteCode(String inviteCode);
}
