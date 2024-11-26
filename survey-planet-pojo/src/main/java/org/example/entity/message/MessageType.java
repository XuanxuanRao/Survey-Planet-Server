package org.example.entity.message;

import lombok.Getter;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Getter
public enum MessageType {
    /**
     * system message
     */
    SYSTEM,
    /**
     * invite to fill survey
     */
    INVITE,
    /**
     * new submission
     */
    NEW_SUBMISSION;

    private final Integer code = this.ordinal();

    public static MessageType of(String type) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.name().equalsIgnoreCase(type)) {
                return messageType;
            }
        }
        return null;
    }

    public static MessageType of(Integer code) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.code.equals(code)) {
                return messageType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
