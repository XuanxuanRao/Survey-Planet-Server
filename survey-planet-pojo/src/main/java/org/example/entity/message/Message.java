package org.example.entity.message;

import lombok.Data;

import java.util.HashMap;

@Data
public abstract class Message {
    private Long mid;
    private Long receiverId;
    private MessageType type;
    /**
     * 是否已读
     */
    private Boolean isRead;


    /**
     * 将 Message 的属性转化为发送的文本内容
     * <p> 这里发送的只是消息的概览内容
     * @return 要发送的消息概览
     */
    public abstract String toText();

    public abstract String toText(HashMap<String, String> params);
}
