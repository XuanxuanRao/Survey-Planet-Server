package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class BadQuestionException extends BusinessException {
    public BadQuestionException(String message) {
        super(message);
    }

    public BadQuestionException() {}
}
