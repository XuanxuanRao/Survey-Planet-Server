package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class QuestionNotFoundException extends BusinessException {

    public QuestionNotFoundException(String message) {
        super(message);
    }

    public QuestionNotFoundException() {
    }
}
