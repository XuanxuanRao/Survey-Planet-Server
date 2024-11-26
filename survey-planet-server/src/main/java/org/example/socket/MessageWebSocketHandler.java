package org.example.socket;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.mapper.MessageMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
@Slf4j
public class MessageWebSocketHandler extends TextWebSocketHandler {

    @Resource
    private MessageMapper messageMapper;


    /**
     * Store the WebSocket session of each user
     * <p> Key: userId, Value: WebSocketSession
     */
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        userSessions.put(userId, session);
        log.info("User {} connected", userId);

        int unreadMessageCount = messageMapper.getUnreadMessageByUid(userId).size();
        if (unreadMessageCount > 0) {
            try {
                session.sendMessage(new TextMessage("You have " + unreadMessageCount + " unread messages"));
            } catch (IOException e) {
                log.error("Failed to send message to user {}", userId, e);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理 WebSocket 消息
        Long userId = (Long) session.getAttributes().get("userId");
        System.out.println("Received message from " + userId + ": " + message.getPayload());
        session.sendMessage(new TextMessage("Message received"));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        userSessions.remove(userId);
        log.info("User {} disconnected with code {}", userId, status.getCode());
    }


    public void sendMessage(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("Sent message to user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send message to user {}", userId, e);
            }
        }
    }

}
