package com.itheima.msg.service;

import com.itheima.msg.dto.MsgVO;
import com.itheima.msg.dto.PublishMsgRequest;
import com.itheima.msg.entity.Msg;

import java.util.List;

public interface MsgService {

    Msg publish(PublishMsgRequest request);

    List<MsgVO> getMyMessages(Integer userId);

    int getUnreadCount(Integer userId);

    void markAsRead(Integer id, Integer userId);

    void markAllAsRead(Integer userId);
}