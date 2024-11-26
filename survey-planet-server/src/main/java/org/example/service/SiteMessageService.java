package org.example.service;

import org.example.entity.message.InviteMessage;
import org.example.entity.message.NewSubmissionMessage;
import org.example.entity.message.SystemMessage;

public interface SiteMessageService {
    void send(InviteMessage message);
    void send(SystemMessage message);
    void send(NewSubmissionMessage message);
}
