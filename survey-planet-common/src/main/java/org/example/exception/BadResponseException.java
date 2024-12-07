package org.example.exception;

import java.util.HashMap;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
public class BadResponseException extends BusinessException {
    /**
     * 错误信息，以问题为单位
     * <p> key: 问题ID, value: 该题回答的错误信息
     */
    private final HashMap<Long, String> errors = new HashMap<>();

    public BadResponseException() {
    }

    public BadResponseException(String msg) {
        super(msg);
    }

    public BadResponseException(Long qid, String msg) {
        super("Bad response for question " + qid + ": " + msg);
        errors.put(qid, msg);
    }

}
