package org.example.roommanager.controller;

import jakarta.servlet.http.HttpSession;
import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.dto.*;
import org.example.roommanager.exception.BusinessException;
import org.example.roommanager.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    public static final String SESSION_USER_KEY = "LOGIN_USER";

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request, HttpSession session) {
        UserResponse user = userService.login(request);
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole());
        session.setAttribute(SESSION_USER_KEY, loginUser);
        return user;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        var loginUser = LoginUserContext.requireLogin();
        return new UserResponse(loginUser.getId(), loginUser.getUsername(), loginUser.getRole());
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
}