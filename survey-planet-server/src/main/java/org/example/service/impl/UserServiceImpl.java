package org.example.service.impl;

import jakarta.annotation.Resource;

import org.example.context.BaseContext;
import org.example.dto.user.UserLoginDTO;
import org.example.dto.user.UserRegisterDTO;
import org.example.dto.user.UserResetDTO;
import org.example.entity.User;
import org.example.exception.*;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.example.service.VerificationCodeService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;


@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private VerificationCodeService verificationCodeService;

    @Override
    public User getById(Long uid) {
        return userMapper.getById(uid);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.getByEmail(email);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();
        // 检查参数是否合法，用户名和邮箱有且只有一个存在
        if (!StringUtils.hasLength(username) && !StringUtils.hasLength(email)) {
            throw new IllegalRequestException(
                UserServiceImpl.class.getName() + ".login",
                "USERNAME_AND_EMAIL_BOTH_NULL"
            );
        }
        if (StringUtils.hasLength(username) && StringUtils.hasLength(email)) {
            throw new IllegalRequestException(
                UserServiceImpl.class.getName() + ".login",
                "USERNAME_AND_EMAIL_BOTH_EXIST"
            );
        }

        User user = StringUtils.hasLength(username) ? userMapper.getByUsername(username) : userMapper.getByEmail(email);
        // 如果用户不存在
        if (user == null) {
            throw new UserNotFoundException("USERNAME_NOT_FOUND");
        }
        // 如果密码不正确
        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
            throw new PasswordErrorException("PASSWORD_ERROR");
        }

        return user;
    }

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        if (userMapper.getByUsername(userRegisterDTO.getUsername()) != null) {
            throw new DuplicateUserInformationException("USERNAME_EXIST");
        }
        if (userMapper.getByEmail(userRegisterDTO.getEmail()) != null) {
            throw new DuplicateUserInformationException("EMAIL_EXIST");
        }
        if (!verificationCodeService.query(userRegisterDTO.getEmail(), userRegisterDTO.getCode())) {
            throw new VerificationCodeErrorException("VERIFICATION_CODE_ERROR");
        }

        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .email(userRegisterDTO.getEmail())
                .password(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes())) // MD5加密
                .build();
        userMapper.insert(user);
        verificationCodeService.delete(userRegisterDTO.getEmail());
        return user;
    }

    @Override
    public User resetPassword(UserResetDTO userResetDTO) {
        User user = userMapper.getByEmail(userResetDTO.getEmail());
        // 如果用户不存在
        if (user == null) {
            throw new UserNotFoundException("EMAIL_NOT_FOUND");
        }
        // 如果验证码错误
        if (!verificationCodeService.query(userResetDTO.getEmail(), userResetDTO.getCode())) {
            throw new VerificationCodeErrorException("VERIFICATION_CODE_ERROR");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(userResetDTO.getPassword().getBytes()));

        verificationCodeService.delete(userResetDTO.getEmail());
        userMapper.update(user);
        return user;
    }

    @Override
    public void update(User user) {
        user.setUid(BaseContext.getCurrentId());
        if (StringUtils.hasLength(user.getPassword())) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        userMapper.update(user);
    }

}
