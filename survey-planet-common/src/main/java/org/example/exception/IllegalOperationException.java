package org.example.exception;

public class IllegalOperationException extends BusinessException {
    public IllegalOperationException() {
    }

    public IllegalOperationException(String msg) {
        super(msg);
    }
}
