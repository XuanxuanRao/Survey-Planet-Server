package org.example.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.Result.Result;
import org.example.exception.BusinessException;
import org.example.exception.IllegalRequestException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result<String> exceptionHandler(BusinessException ex) {
        log.info("Business Exception: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result<String> exceptionHandler(IllegalRequestException ex) {
        log.error("Illegal Request captured in {}: {}", ex.getLocation(), ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
