package org.example.service;

import org.example.entity.message.InviteMessage;
import org.example.entity.message.NewSubmissionMessage;
import org.example.entity.message.SystemMessage;
import org.example.vo.MessageVO;

public interface SiteMessageService {
    void send(InviteMessage message);
    void send(SystemMessage message);
    void send(NewSubmissionMessage message);
    MessageVO getMessage(Long mid);
    void setUnread(Long mid);
}
