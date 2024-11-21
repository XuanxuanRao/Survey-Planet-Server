package org.example.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.example.result.Result;
import org.example.annotation.ControllerLog;
import org.example.context.BaseContext;
import org.example.dto.user.UserLoginDTO;
import org.example.dto.user.UserRegisterDTO;
import org.example.dto.user.UserResetDTO;
import org.example.entity.User;
import org.example.properties.JwtProperties;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.vo.user.UserLoginVO;
import org.example.vo.user.UserResetVO;
import org.example.vo.user.UserRegisterVO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private JwtProperties jwtProperties;

    @GetMapping("/user")
    public Result<User> get() {
        return Result.success(userService.getById(BaseContext.getCurrentId()));
    }

    @PostMapping("/login")
    @ControllerLog(name = "login", intoDB = true)
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        User user = userService.login(loginDTO);

        // 登录成功，下发JWT
        String JWT = JwtUtil.generateJwt(jwtProperties.getSecretKey(), jwtProperties.getTtl(), new HashMap<>() {{
            put("id", user.getUid());
        }});

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .uid(user.getUid())
                .token(JWT)
                .build();
        return Result.success(userLoginVO);
    }

    @PostMapping("/register")
    @ControllerLog(name = "register", intoDB = true)
    public Result<UserRegisterVO> register(@RequestBody UserRegisterDTO userDTO) {
        User user = userService.register(userDTO);

        UserRegisterVO userRegisterVO = UserRegisterVO.builder()
                .uid(userService.getByUsername(user.getUsername()).getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        log.info("{}({}) registered successfully", user.getUsername(), user.getEmail());
        return Result.success(userRegisterVO);
    }


    @PutMapping("/reset")
    @ControllerLog(name = "resetPassword", intoDB = true)
    public Result<UserResetVO> reset(@RequestBody UserResetDTO userResetDTO) {
        User user = userService.resetPassword(userResetDTO);

        UserResetVO userResetVO = UserResetVO.builder()
                .uid(user.getUid())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        log.info("{}({}) reset password successfully", user.getUsername(), user.getEmail());
        return Result.success(userResetVO);
    }

    @PutMapping("/user")
    @ControllerLog(name = "updateUserInform")
    public Result<Void> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

}
