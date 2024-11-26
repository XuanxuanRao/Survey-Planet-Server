package org.example.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.message.InviteMessage;
import org.example.entity.message.NewSubmissionMessage;
import org.example.entity.message.SystemMessage;
import org.example.mapper.MessageMapper;
import org.example.service.SiteMessageService;
import org.example.socket.MessageWebSocketHandler;
import org.springframework.stereotype.Service;


/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Service
@Slf4j
public class SiteMessageServiceImpl implements SiteMessageService {
    @Resource
    private MessageWebSocketHandler messageWebSocketHandler;

    @Resource
    private MessageMapper messageMapper;

    @Override
    public void send(InviteMessage message) {
        messageMapper.insertInviteMessage(message);
        messageWebSocketHandler.sendMessage(message.getSenderId(), message.toText());
    }

    @Override
    public void send(SystemMessage message) {
        messageMapper.insertSystemMessage(message);
        messageWebSocketHandler.sendMessage(message.getReceiverId(), message.toText());
    }

    @Override
    public void send(NewSubmissionMessage message) {
        messageMapper.insertNewSubmissionMessage(message);
        messageWebSocketHandler.sendMessage(message.getReceiverId(), message.toText());
    }


}
