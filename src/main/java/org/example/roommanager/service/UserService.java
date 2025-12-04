package org.example.roommanager.service;

import org.example.roommanager.dto.LoginRequest;
import org.example.roommanager.dto.RegisterRequest;
import org.example.roommanager.dto.UserResponse;

public interface UserService {

    UserResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse getById(Long id);
}