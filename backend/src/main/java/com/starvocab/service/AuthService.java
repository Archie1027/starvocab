package com.starvocab.service;

import com.starvocab.dto.LoginRequest;
import com.starvocab.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 昵称 + 密码登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新令牌
     */
    LoginResponse refresh(String refreshToken);
}
