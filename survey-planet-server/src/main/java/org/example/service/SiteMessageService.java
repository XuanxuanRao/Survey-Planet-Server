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
}
