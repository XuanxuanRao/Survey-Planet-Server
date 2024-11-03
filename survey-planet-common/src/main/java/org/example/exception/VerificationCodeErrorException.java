package org.example.exception;

public class VerificationCodeErrorException extends BusinessException {
    public VerificationCodeErrorException() {
    }

    public VerificationCodeErrorException(String msg) {
        super(msg);
    }
}
