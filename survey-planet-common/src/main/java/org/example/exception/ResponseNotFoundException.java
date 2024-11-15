package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class ResponseNotFoundException extends BusinessException {
    public ResponseNotFoundException() {}

    public ResponseNotFoundException(String message) {
        super(message);
    }
}
