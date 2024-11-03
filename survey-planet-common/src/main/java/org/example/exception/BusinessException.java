package org.example.exception;

/**
 * 业务异常类，通常由逻辑错误引起，属于正常情况。
 * 在项目中可以通过 GlobalExceptionHandler 捕获该异常来处理，需要对前端响应一些提示性信息。
 */
public abstract class BusinessException extends RuntimeException {
    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
