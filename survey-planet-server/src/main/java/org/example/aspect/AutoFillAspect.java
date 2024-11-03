package org.example.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.annotation.AutoFill;
import org.example.constant.AutoFillConstant;
import org.example.enumeration.OperationType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Aspect
@Component
public class AutoFillAspect {

    private static final Map<OperationType, Consumer<Object>> operationHandlers = new HashMap<>() {{
        put(OperationType.INSERT, AutoFillAspect::handleInsert);
        put(OperationType.UPDATE, AutoFillAspect::handleUpdate);
    }};

    /**
     * 切入点
     */
    @Pointcut("execution(* org.example.mapper.*.*(..)) && @annotation(org.example.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获得数据库操作类型
        OperationType type = signature.getMethod().getAnnotation(AutoFill.class).value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        operationHandlers.get(type).accept(args[0]);
    }

    private static void handleInsert(Object entity) {
        try {
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setCreateTime.invoke(entity, LocalDateTime.now());
            setUpdateTime.invoke(entity, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("method not found in entity for auto fill");
        }
    }

    private static void handleUpdate(Object entity) {
        try {
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            setUpdateTime.invoke(entity, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("method not found in entity for auto fill");
        }
    }
}
