package org.example.config;

import jakarta.annotation.Resource;
import org.example.converter.MessageTypeConverter;
import org.example.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private static final String[] EXCLUDE_PATHS = {
            "/api/login",
            "/api/register",
            "/api/common/email/code",
            "/api/reset",
            "/api/admin/survey/clone"
    };

    // 注册拦截器
    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private MessageTypeConverter messageTypeConverter;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")             // 拦截所有请求
                .excludePathPatterns(EXCLUDE_PATHS);    // 放行登录、注册、发送验证码、重置密码接口
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://59.110.163.198")
                .allowedOriginPatterns("http://59.110.163.198:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(messageTypeConverter);
    }


}
