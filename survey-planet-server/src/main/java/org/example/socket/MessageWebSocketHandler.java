package org.example.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.mapper.MessageMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
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

        int unreadMessageCount = messageMapper.getMessageByUid(userId, false, null).size();
        if (unreadMessageCount > 0) {
            sendMessage(userId, new HashMap<>() {{
                put("content", "You have " + unreadMessageCount + " unread message(s)!");
            }});
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

    /**
     * Send message to user
     * @param userId user id
     * @param message a hashmap representing the json message object
     * <p> Key mid is the message id, content is the message content
     */
    public void sendMessage(Long userId, HashMap<String, Object> message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                log.info("Sent message to user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send message to user {}", userId, e);
            }
        }
    }

}
