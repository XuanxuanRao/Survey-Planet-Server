package org.example.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ControllerLog {
    String name();
    // 该条日志是否存入数据库
    boolean intoDB() default false;
}
