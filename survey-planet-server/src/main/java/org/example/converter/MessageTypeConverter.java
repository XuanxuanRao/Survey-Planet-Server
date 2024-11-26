package org.example.converter;

import org.example.entity.message.MessageType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Component
public class MessageTypeConverter implements Converter<String, MessageType> {
    @Override
    public MessageType convert(String source) {
        return MessageType.of(source);
    }
}
