package org.example.service;


import org.example.dto.user.UserLoginDTO;
import org.example.dto.user.UserRegisterDTO;
import org.example.dto.user.UserResetDTO;
import org.example.entity.User;

public interface UserService {
    User getById(Long uid);

    User getByUsername(String username);

    User login(UserLoginDTO userLoginDTO);

    User register(UserRegisterDTO userRegisterDTO);

    User resetPassword(UserResetDTO userResetDTO);

    User getByEmail(String email);

    void update(User user);
}
