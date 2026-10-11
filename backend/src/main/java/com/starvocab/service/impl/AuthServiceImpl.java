package com.starvocab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.dto.LoginRequest;
import com.starvocab.dto.LoginResponse;
import com.starvocab.entity.StarUser;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.service.AuthService;
import com.starvocab.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StarUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 根据昵称查询用户
        StarUser user = userMapper.selectOne(
                new LambdaQueryWrapper<StarUser>().eq(StarUser::getNickname, request.getNickname())
        );
        if (user == null) {
            throw new IllegalArgumentException("星辰尚未对齐，请重试");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("星辰尚未对齐，请重试");
        }

        // 3. 签发 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getNickname());

        return LoginResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200)
                .build();
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        if (jwtUtil.isExpired(refreshToken)) {
            throw new IllegalArgumentException("星轨已过期，请重新登录");
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星辰尚未对齐，请重试");
        }
        String newAccess = jwtUtil.generateAccessToken(user.getId(), user.getNickname());
        String newRefresh = jwtUtil.generateRefreshToken(user.getId(), user.getNickname());
        return LoginResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .expiresIn(7200)
                .build();
    }
}
