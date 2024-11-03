package org.example.exception;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class ResponseNotFinishedException extends BusinessException {
    public ResponseNotFinishedException() {
        super();
    }

    public ResponseNotFinishedException(String msg) {
        super(msg);
    }
}
