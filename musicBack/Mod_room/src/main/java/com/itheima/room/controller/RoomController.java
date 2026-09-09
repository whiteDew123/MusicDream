package com.itheima.room.controller;

import com.itheima.domain.common.Result;
import com.itheima.room.dto.PlaylistAddDTO;
import com.itheima.room.dto.PlaylistSortDTO;
import com.itheima.room.dto.RoomCreateDTO;
import com.itheima.room.dto.RoomUpdateDTO;
import com.itheima.room.dto.SkipVoteDTO;
import com.itheima.room.dto.TransferDTO;
import com.itheima.room.mapper.RoomMemberMapper;
import com.itheima.room.mapper.RoomMemberSessionMapper;
import com.itheima.room.mapper.RoomStatsMapper;
import com.itheima.room.mapper.UserMapper;
import com.itheima.room.service.RoomMessageService;
import com.itheima.room.service.RoomPlaylistService;
import com.itheima.room.service.RoomService;
import com.itheima.room.service.RoomVoteService;
import com.itheima.room.vo.RoomDetailVO;
import com.itheima.room.vo.RoomMessageVO;
import com.itheima.room.vo.RoomPlaylistItemVO;
import com.itheima.room.vo.RoomVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 一起听·播放室 房间接口
 * <p>
 * 网关路由：/api/room/** → StripPrefix=1 → /room/**
 * <p>
 * 除 /room/invite/{code} 外需登录（网关校验 JWT 后通过 X-User-Id 头透传用户ID）。
 */
@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;
    private final RoomPlaylistService roomPlaylistService;
    private final RoomMessageService roomMessageService;
    private final RoomVoteService roomVoteService;
    private final RoomStatsMapper statsMapper;
    private final RoomMemberMapper memberMapper;
    private final RoomMemberSessionMapper sessionMapper;
    private final UserMapper userMapper;

    public RoomController(RoomService roomService,
                          RoomPlaylistService roomPlaylistService,
                          RoomMessageService roomMessageService,
                          RoomVoteService roomVoteService,
                          RoomStatsMapper statsMapper,
                          RoomMemberMapper memberMapper,
                          RoomMemberSessionMapper sessionMapper,
                          UserMapper userMapper) {
        this.roomService = roomService;
        this.roomPlaylistService = roomPlaylistService;
        this.roomMessageService = roomMessageService;
        this.roomVoteService = roomVoteService;
        this.statsMapper = statsMapper;
        this.memberMapper = memberMapper;
        this.sessionMapper = sessionMapper;
        this.userMapper = userMapper;
    }

    // ======================== 房间 ========================

    /**
     * 创建房间
     *
     * @param dto    请求体
     * @param userId 当前登录用户ID（X-User-Id）
     * @return 新房间详情
     */
    @PostMapping("/create")
    public Result<RoomDetailVO> create(@RequestBody RoomCreateDTO dto,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success("创建成功", roomService.create(dto, userId));
    }

    /**
     * 查询房间详情
     *
     * @param id     房间ID
     * @param userId 当前登录用户ID（可选，用于标记是否成员）
     * @return 房间详情
     */
    @GetMapping("/{id}")
    public Result<RoomDetailVO> detail(@PathVariable Long id,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(roomService.getDetail(id, userId));
    }

    /**
     * 查询房间列表（公开房间 + 我加入的房间）
     *
     * @param userId 当前登录用户ID
     * @return 房间列表
     */
    @GetMapping("/list")
    public Result<List<RoomVO>> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(roomService.listRooms(userId));
    }

    /**
     * 修改房间（仅房主）
     *
     * @param id     房间ID
     * @param dto    请求体（字段可空）
     * @param userId 当前登录用户ID
     * @return 更新后的房间详情
     */
    @PutMapping("/{id}")
    public Result<RoomDetailVO> update(@PathVariable Long id,
                                       @RequestBody RoomUpdateDTO dto,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        dto.setId(id);
        return Result.success("修改成功", roomService.update(dto, userId));
    }

    /**
     * 关闭房间（仅房主）
     *
     * @param id     房间ID
     * @param userId 当前登录用户ID
     * @return 无数据
     */
    @DeleteMapping("/{id}")
    public Result<Void> close(@PathVariable Long id,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomService.close(id, userId);
        return Result.success();
    }

    /**
     * 加入房间
     *
     * @param id     房间ID
     * @param userId 当前登录用户ID
     * @return 房间详情
     */
    @PostMapping("/{id}/join")
    public Result<RoomDetailVO> join(@PathVariable Long id,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success("已加入房间", roomService.join(id, userId));
    }

    /**
     * 离开房间（房主离开会解散房间）
     *
     * @param id     房间ID
     * @param userId 当前登录用户ID
     * @return 无数据
     */
    @PostMapping("/{id}/leave")
    public Result<Void> leave(@PathVariable Long id,
                              @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomService.leave(id, userId);
        return Result.success();
    }

    /**
     * 转让房主（仅房主）
     *
     * @param id     房间ID
     * @param dto    请求体
     * @param userId 当前登录用户ID
     * @return 无数据
     */
    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable Long id,
                                 @RequestBody TransferDTO dto,
                                 @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomService.transfer(id, userId, dto.getUserId());
        return Result.success("转让成功", null);
    }

    /**
     * 移出成员（仅房主）
     *
     * @param id          房间ID
     * @param targetUserId 被移出的用户ID
     * @param userId      当前登录用户ID
     * @return 无数据
     */
    @DeleteMapping("/{id}/kick/{targetUserId}")
    public Result<Void> kick(@PathVariable Long id,
                             @PathVariable Long targetUserId,
                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomService.kick(id, userId, targetUserId);
        return Result.success();
    }

    /**
     * 通过邀请码查询房间（公开接口：网关白名单 /api/room/invite/**）
     *
     * @param inviteCode 邀请码
     * @param userId     当前登录用户ID（可选）
     * @return 房间详情
     */
    @GetMapping("/invite/{inviteCode}")
    public Result<RoomDetailVO> invite(@PathVariable String inviteCode,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(roomService.getByInviteCode(inviteCode));
    }

    // ======================== 歌单 ========================

    /**
     * 查询房间歌单
     *
     * @param id     房间ID
     * @param userId 当前登录用户ID（可选）
     * @return 歌单列表
     */
    @GetMapping("/{id}/playlist")
    public Result<List<RoomPlaylistItemVO>> playlist(@PathVariable Long id,
                                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(roomPlaylistService.getList(id, userId));
    }

    /**
     * 添加歌曲到歌单
     *
     * @param id     房间ID
     * @param dto    请求体 { musicId }
     * @param userId 当前登录用户ID
     * @return 无数据
     */
    @PostMapping("/{id}/playlist/add")
    public Result<Void> addPlaylist(@PathVariable Long id,
                                    @RequestBody PlaylistAddDTO dto,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomPlaylistService.add(id, dto.getMusicId(), userId);
        return Result.success("已添加到歌单", null);
    }

    /**
     * 从歌单移除歌曲
     *
     * @param id      房间ID
     * @param musicId 歌曲ID
     * @param userId  当前登录用户ID
     * @return 无数据
     */
    @DeleteMapping("/{id}/playlist/{musicId}")
    public Result<Void> removePlaylist(@PathVariable Long id,
                                       @PathVariable Long musicId,
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomPlaylistService.remove(id, musicId, userId);
        return Result.success();
    }

    /**
     * 歌单排序
     *
     * @param id     房间ID
     * @param dto    请求体 { items: [{ musicId, sortOrder }] }
     * @param userId 当前登录用户ID
     * @return 无数据
     */
    @PutMapping("/{id}/playlist/sort")
    public Result<Void> sortPlaylist(@PathVariable Long id,
                                     @RequestBody PlaylistSortDTO dto,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomPlaylistService.sort(id, dto, userId);
        return Result.success();
    }

    // ======================== 消息 ========================

    /**
     * 查询房间消息（支持 after 参数补齐遗漏）
     *
     * @param id       房间ID
     * @param after    上次收到的 seq，返回 seq 大于该值的消息
     * @param userId   当前登录用户ID（可选）
     * @return 消息列表
     */
    @GetMapping("/{id}/messages")
    public Result<List<RoomMessageVO>> messages(@PathVariable Long id,
                                                @RequestParam(value = "after", required = false) Integer after,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(roomMessageService.getMessages(id, after));
    }

    // ======================== 切歌投票 ========================

    /**
     * 发起切歌投票
     *
     * @param id     房间ID
     * @param dto    请求体 { musicId }
     * @param userId 当前登录用户ID
     * @return true 表示已发起
     */
    @PostMapping("/{id}/skip-vote")
    public Result<Boolean> skipVote(@PathVariable Long id,
                                    @RequestBody SkipVoteDTO dto,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        roomVoteService.skipVote(id, dto.getMusicId(), userId);
        return Result.success("已发起切歌投票", true);
    }

    /**
     * 附议切歌投票
     *
     * @param id     房间ID
     * @param dto    请求体 { musicId }
     * @param userId 当前登录用户ID
     * @return true 表示已成功切歌，false 表示仍需等待更多附议
     */
    @PostMapping("/{id}/skip-vote/agree")
    public Result<Boolean> agreeVote(@PathVariable Long id,
                                     @RequestBody SkipVoteDTO dto,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        boolean skipped = roomVoteService.agreeVote(id, dto.getMusicId(), userId);
        return Result.success(skipped ? "切歌成功" : "已附议，等待更多成员", skipped);
    }

    // ======================== 统计 ========================

    /**
     * 查询房间统计：总观看人数、峰值在线、累计观看分钟
     * GET /room/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public Result<Object> stats(@PathVariable Long id) {
        // 总观看：room_member COUNT DISTINCT user_id
        long totalViewers = memberMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.itheima.room.entity.RoomMember>()
                        .eq("room_id", id).select("DISTINCT user_id"));
        // 当前在线
        long onlineNow = memberMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.itheima.room.entity.RoomMember>()
                        .eq("room_id", id).eq("is_online", 1));
        // room_stats 表的峰值 + 累计时长
        java.util.Map<String, Object> stat = new java.util.HashMap<>();
        stat.put("totalViewers", totalViewers);
        stat.put("onlineNow", onlineNow);
        com.itheima.room.entity.RoomStats rs = statsMapper.selectById(id);
        stat.put("peakOnline", rs == null ? 0 : rs.getPeakOnline());
        stat.put("totalWatchMinutes", rs == null ? 0L : rs.getTotalWatchMinutes());
        return Result.success(stat);
    }

    /**
     * 查询房间最近进出记录
     * GET /room/{id}/sessions?limit=50
     */
    @GetMapping("/{id}/sessions")
    public Result<Object> sessions(@PathVariable Long id,
                                    @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<com.itheima.room.entity.RoomMemberSession> sessions = sessionMapper.selectRecentByRoom(id, Math.min(limit, 200));
        // 联表用户信息：把 userId → username + imageUrl
        List<Long> userIds = sessions.stream().map(com.itheima.room.entity.RoomMemberSession::getUserId).distinct().toList();
        java.util.Map<Integer, com.itheima.domain.entity.User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = new java.util.HashMap<>(userMapper.selectBatchIds(
                    userIds.stream().map(Long::intValue).toList())
                    .stream().collect(java.util.stream.Collectors.toMap(
                            com.itheima.domain.entity.User::getId, u -> u)));
        }
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.itheima.room.entity.RoomMemberSession s : sessions) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("userId", s.getUserId());
            com.itheima.domain.entity.User u = userMap.get(s.getUserId().intValue());
            if (u != null) {
                item.put("username", u.getUsername());
                item.put("avatar", u.getImageUrl());
            }
            item.put("enterTime", s.getEnterTime());
            item.put("leaveTime", s.getLeaveTime());
            item.put("durationSec", s.getDurationSec());
            result.add(item);
        }
        return Result.success(result);
    }
}
