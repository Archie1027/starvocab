package com.starvocab.controller;

import com.starvocab.common.Result;
import com.starvocab.dto.UpdatePasswordRequest;
import com.starvocab.dto.UserProfileRequest;
import com.starvocab.entity.StarUser;
import com.starvocab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 - 个人资料 / 修改密码
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<StarUser> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<StarUser> updateProfile(Authentication authentication,
                                          @RequestBody UserProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(Authentication authentication,
                                       @Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updatePassword(userId, request);
        return Result.ok("星图密令已更新", null);
    }

    @GetMapping("/preferences")
    public Result<com.starvocab.dto.PreferencesDTO> preferences(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getPreferences(userId));
    }

    @PutMapping("/preferences")
    public Result<com.starvocab.dto.PreferencesDTO> updatePreferences(Authentication authentication,
                                                                      @RequestBody com.starvocab.dto.PreferencesDTO request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok("学习偏好已同步", userService.updatePreferences(userId, request));
    }
}
