package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.AuthResponse;
import com.diecastcollector.api.dto.SocialLoginRequest;
import com.diecastcollector.api.enums.AuthProvider;
import com.diecastcollector.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@Valid @RequestBody SocialLoginRequest request) {
        return authService.login(AuthProvider.GOOGLE, request.idToken());
    }

    @PostMapping("/apple")
    public AuthResponse loginWithApple(@Valid @RequestBody SocialLoginRequest request) {
        return authService.login(AuthProvider.APPLE, request.idToken());
    }
}
