package org.example.exception;

public class SurveyNotFoundException extends BusinessException {
    public SurveyNotFoundException() {}

    public SurveyNotFoundException(String msg) {
        super(msg);
    }
}
