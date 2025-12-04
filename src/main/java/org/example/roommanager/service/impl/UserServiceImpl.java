package org.example.roommanager.service.impl;

import org.example.roommanager.dto.LoginRequest;
import org.example.roommanager.dto.RegisterRequest;
import org.example.roommanager.dto.UserResponse;
import org.example.roommanager.entity.User;
import org.example.roommanager.exception.BusinessException;
import org.example.roommanager.repository.UserRepository;
import org.example.roommanager.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("LOGIN_FAILED", "用户名或密码错误"));

        // 简化：明文密码比较，答辩时可以说明后续可改为加密存储
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("LOGIN_FAILED", "用户名或密码错误");
        }

        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .role("USER") // 注册默认普通用户
                .createTime(LocalDateTime.now())
                .build();
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    @Cacheable(cacheNames = "userById", key = "#id")
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole()))
                .orElse(null);
    }
}