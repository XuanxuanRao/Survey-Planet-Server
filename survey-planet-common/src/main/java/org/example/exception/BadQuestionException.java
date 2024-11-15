package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class BadQuestionException extends RuntimeException {
    public BadQuestionException(String message) {
        super(message);
    }

    public BadQuestionException() {}
}
