package org.example.interceptor;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.example.result.Result;
import org.example.context.BaseContext;
import org.example.properties.JwtProperties;
import org.example.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    // 在请求处理之前进行调用, Controller方法调用之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("request {}", request.getRequestURL().toString());
        String token = request.getHeader(jwtProperties.getTokenName());
        // check the token
        if (!StringUtils.hasLength(token)) {
            log.info("Not logged in, url: {}", request.getRequestURL());
            String notLogin = JSONObject.toJSONString(Result.error("NOT_LOGIN"));
            response.getWriter().write(notLogin);
            return false;
        }
        try {
            // validate the token
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = Long.parseLong(claims.get("id").toString());
            log.info("Current user id: {}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.info("Token validation failed, url: {}", request.getRequestURL());
            String notLogin = JSONObject.toJSONString(Result.error("NOT_LOGIN"));
            response.getWriter().write(notLogin);
            return false;
        }
    }
}
