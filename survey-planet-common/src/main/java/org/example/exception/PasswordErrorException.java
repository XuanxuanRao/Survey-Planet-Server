package org.example.exception;

public class PasswordErrorException extends BusinessException {
    public PasswordErrorException() {
    }

    public PasswordErrorException(String msg) {
        super(msg);
    }
}
