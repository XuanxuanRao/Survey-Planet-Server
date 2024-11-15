package org.example.aspect;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.annotation.ControllerLog;
import org.example.entity.LogEntry;
import org.example.exception.BusinessException;
import org.example.mapper.LogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenxuanrao06@gmail.com
 * @Description: Aspect for logging.
 * <p> Log relevant information when requesting the Controller layer interface
 */
@Slf4j
@Component
@Aspect
public class LogAspect {

    @Resource
    private LogRepository logRepository;

    private final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    @Pointcut("execution(* org.example.controller.*.*(..))")
    public void logPointCut() {
    }

    /**
     * 记录接口的调用信息
     *
     * @param joinPoint     切入点
     * @param controllerLog 接口的日志注解
     */
    @Before(value = "logPointCut() && @annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, ControllerLog controllerLog) {
        // 接收到请求
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        HttpServletRequest request = sra.getRequest();
        // 记录请求内容，threadInfo存储所有内容
        Map<String, Object> threadInfo = new HashMap<>();
        threadInfo.put("url", request.getRequestURL());
        threadInfo.put("uri", request.getRequestURI());
        threadInfo.put("httpMethod", request.getMethod());
        threadInfo.put("ip", request.getRemoteAddr());
        threadInfo.put("classMethod", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        threadInfo.put("args", Arrays.toString(joinPoint.getArgs()));
        threadInfo.put("userAgent", request.getHeader("User-Agent"));
        threadInfo.put("methodName", controllerLog.name());
        threadInfo.put("startTime", LocalDateTime.now());
        threadLocal.set(threadInfo);

        log.info("REQUEST ==> " + "(" + threadInfo.get("httpMethod") + ")" + threadInfo.get("uri") + " invoke method " + threadInfo.get("classMethod") + "() with arguments " + threadInfo.get("args"));
        log.info("REQUEST ==> " + "ip: " + threadInfo.get("ip") + ", userAgent: " + threadInfo.get("userAgent"));
    }

    /**
     * 响应成功后，记录接口的返回信息
     *
     * @param controllerLog 接口的日志注解
     * @param ret           返回值
     */
    @AfterReturning(value = "logPointCut() && @annotation(controllerLog)", returning = "ret")
    public void doAfterReturning(ControllerLog controllerLog, Object ret) throws Throwable {
        Map<String, Object> threadInfo = threadLocal.get();
        threadInfo.put("result", ret);
        threadInfo.put("endTime", LocalDateTime.now());
        threadInfo.put("takeTime", Duration.between((LocalDateTime) threadInfo.get("startTime"), (LocalDateTime) threadInfo.get("endTime")).toMillis());

        log.info("RESPONSE ==> " + ret + ", take time: " + threadInfo.get("takeTime"));

        if (controllerLog.intoDB()) {
            insertLog(threadInfo);
        }
    }

    /**
     * 异常处理
     * @param throwable
     */
    @AfterThrowing(value = "logPointCut()", throwing = "throwable")
    public void doAfterThrowing(Throwable throwable) {

        if (throwable instanceof BusinessException) {
            return;
        }

        RequestAttributes ra = RequestContextHolder.getRequestAttributes();

        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        HttpServletRequest request = sra.getRequest();

        // 异常信息
        log.error("Exception occurred while processing request [{}]: {}", request.getRequestURI(), throwable.getMessage(), throwable);
    }

    @Async("taskExecutor")
    protected void insertLog(Map<String, Object> threadInfo) {
        LogEntry logEntry = new LogEntry();
        logEntry.setUrl(((StringBuffer) threadInfo.get("url")).toString());
        logEntry.setUri((String) threadInfo.get("uri"));
        logEntry.setHttpMethod((String) threadInfo.get("httpMethod"));
        logEntry.setIp((String) threadInfo.get("ip"));
        logEntry.setClassMethod((String) threadInfo.get("classMethod"));
        logEntry.setArgs((String) threadInfo.get("args"));
        logEntry.setUserAgent((String) threadInfo.get("userAgent"));
        logEntry.setMethodName((String) threadInfo.get("methodName"));
        logEntry.setResult(threadInfo.get("result").toString());
        logEntry.setStartTime((LocalDateTime) threadInfo.get("startTime"));
        logEntry.setEndTime((LocalDateTime) threadInfo.get("endTime"));
        logEntry.setTakeTime((Long) threadInfo.get("takeTime"));
        logRepository.save(logEntry);
    }

}
