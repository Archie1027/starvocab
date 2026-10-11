package com.starvocab.dto;

import lombok.Data;

/**
 * 刷新 Token 请求 DTO
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
