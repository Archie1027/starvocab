package com.starvocab.controller;

import com.starvocab.common.Result;
import com.starvocab.dto.LoginRequest;
import com.starvocab.dto.LoginResponse;
import com.starvocab.dto.RefreshTokenRequest;
import com.starvocab.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - 登录 / 刷新令牌
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return Result.ok(authService.refresh(request.getRefreshToken()));
    }
}
