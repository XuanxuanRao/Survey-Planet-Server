package org.example.exception;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
    }

    public UserNotFoundException(String msg) {
        super(msg);
    }
}
