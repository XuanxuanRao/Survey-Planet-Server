package org.example.service;

import org.example.entity.message.*;
import org.example.vo.MessageVO;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public interface SiteMessageService {
    void send(InviteMessage message);
    void send(SystemMessage message);
    void send(NewSubmissionMessage message);
    MessageVO getMessage(Long mid);
    void setUnread(Long mid);
    void setRead(Long mid);
    List<Message> getMessages(Boolean isRead, MessageType type);
    Integer deleteReadMessageOlderThan(int day, MessageType type);
    /**
     * 从数据库中删除消息
     * @param mid 要删除的消息 ID
     */
    void deleteMessage(Long mid);
}
