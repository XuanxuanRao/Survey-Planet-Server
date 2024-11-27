package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class MessageNotFoundException extends BusinessException {
    public MessageNotFoundException() {
        super();
    }

    public MessageNotFoundException(String msg) {
        super(msg);
    }
}
