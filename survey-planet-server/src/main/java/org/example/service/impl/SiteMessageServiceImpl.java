package org.example.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.LinkConstant;
import org.example.context.BaseContext;
import org.example.entity.User;
import org.example.entity.message.*;
import org.example.entity.response.Response;
import org.example.entity.survey.Survey;
import org.example.exception.MessageNotFoundException;
import org.example.mapper.MessageMapper;
import org.example.mapper.ResponseMapper;
import org.example.mapper.SurveyMapper;
import org.example.mapper.UserMapper;
import org.example.service.SiteMessageService;
import org.example.socket.MessageWebSocketHandler;
import org.example.utils.SharingCodeUtil;
import org.example.vo.MessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


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

    @Resource
    private SurveyMapper surveyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ResponseMapper responseMapper;

    @Override
    public void send(InviteMessage message) {
        messageMapper.insertInviteMessage(message);
        messageWebSocketHandler.sendMessage(message.getSenderId(), new HashMap<>() {{
            put("mid", message.getMid());
            put("content", message.toText());
        }});
    }

    @Override
    public void send(SystemMessage message) {
        messageMapper.insertSystemMessage(message);
        messageWebSocketHandler.sendMessage(message.getReceiverId(), new HashMap<>() {{
            put("mid", message.getMid());
            put("content", message.toText());
        }});
    }

    @Override
    public void send(NewSubmissionMessage message) {
        messageMapper.insertNewSubmissionMessage(message);
        messageWebSocketHandler.sendMessage(message.getReceiverId(), new HashMap<>() {{
            put("mid", message.getMid());
            put("content", message.toText());
        }});
    }

    @Override
    public MessageVO getMessage(Long mid) {
        Message message = messageMapper.getMessageByMid(mid);
        if (message == null) {
            throw new MessageNotFoundException("MESSAGE_NOT_FOUND");
        }
        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(message, messageVO);
        if (message instanceof InviteMessage inviteMessage) {
            User sender = userMapper.getById(inviteMessage.getSenderId());
            messageVO.setSenderName(sender.getUsername());
            Survey survey = surveyMapper.getBySid(inviteMessage.getSid());
            messageVO.setSurveyTitle(survey.getTitle());
            messageVO.setSurveyType(survey.getType());
            messageVO.setSurveyFillLink(LinkConstant.FILL_SURVEY + SharingCodeUtil.encrypt(inviteMessage.getSid()));
        } else if (message instanceof NewSubmissionMessage newSubmissionMessage) {
            Response response = responseMapper.getByRid(newSubmissionMessage.getRid());
            messageVO.setSubmitTime(response.getCreateTime());
            Survey survey = surveyMapper.getBySid(newSubmissionMessage.getSid());
            messageVO.setSubmitLink(LinkConstant.VIEW_SUBMIT + newSubmissionMessage.getRid());
            messageVO.setSurveyTitle(survey.getTitle());
            messageVO.setSurveyType(survey.getType());
            messageVO.setSurveyReportLink(LinkConstant.ANALYSIS_SURVEY + newSubmissionMessage.getSid());
        } else if (message instanceof SystemMessage systemMessage) {
            messageVO.setSenderName("System");
        } else {
            log.error("Unknown message type: {}", message.getClass().getName());
        }
        messageMapper.setRead(mid);
        return messageVO;
    }

    @Override
    public void setUnread(Long mid) {
        messageMapper.setUnread(mid);
    }

    @Override
    public void setRead(Long mid) {
        messageMapper.setRead(mid);
    }

    @Override
    public List<Message> getMessages(Boolean isRead, MessageType type) {
        Long uid = BaseContext.getCurrentId();
        return messageMapper.getMessageByUid(uid, isRead, type);
    }

    @Override
    public Integer deleteReadMessageOlderThan(int day, MessageType type) {
        return messageMapper.getMessages(LocalDateTime.MIN, LocalDateTime.now().minusDays(day), type).stream()
                .filter(Message::getIsRead)
                .mapToInt(message -> {
                    messageMapper.deleteMessage(message.getMid());
                    return 1;
                })
                .sum();
    }
}
