package com.starvocab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
