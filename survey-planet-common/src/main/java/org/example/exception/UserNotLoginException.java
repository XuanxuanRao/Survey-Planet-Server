package org.example.exception;

public class UserNotLoginException extends BusinessException {
    public UserNotLoginException() {
    }

    public UserNotLoginException(String msg) {
        super(msg);
    }
}
