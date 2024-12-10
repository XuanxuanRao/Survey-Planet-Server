package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class BadSurveyException extends BusinessException {
    public BadSurveyException(String message) {
        super(message);
    }

    public BadSurveyException() {
        super();
    }
}
