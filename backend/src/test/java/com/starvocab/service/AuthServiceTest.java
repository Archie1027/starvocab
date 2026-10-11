package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.dto.LoginRequest;
import com.starvocab.dto.LoginResponse;
import com.starvocab.entity.StarUser;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.service.impl.AuthServiceImpl;
import com.starvocab.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AuthService 单元测试
 * 覆盖：正常登录、密码错误、用户不存在、Token 签发
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务单元测试")
class AuthServiceTest {

    @Mock
    private StarUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private StarUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new StarUser();
        mockUser.setId(1L);
        mockUser.setNickname("星旅人");
        mockUser.setPassword("$2a$10$encodedPasswordHash");
        mockUser.setAvatarUrl("/avatars/planet-1.svg");
    }

    @Test
    @DisplayName("正常登录 - 返回 Token 与用户信息")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setNickname("星旅人");
        request.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", mockUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "星旅人")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L, "星旅人")).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getNickname()).isEqualTo("星旅人");
        assertThat(response.getAvatarUrl()).isEqualTo("/avatars/planet-1.svg");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getExpiresIn()).isEqualTo(7200);
    }

    @Test
    @DisplayName("密码错误 - 抛出异常")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setNickname("星旅人");
        request.setPassword("wrong");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", mockUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("星辰尚未对齐，请重试");
    }

    @Test
    @DisplayName("用户不存在 - 抛出异常")
    void login_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setNickname("不存在的昵称");
        request.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("星辰尚未对齐，请重试");
    }

    @Test
    @DisplayName("刷新令牌 - Token 有效时返回新令牌")
    void refresh_success() {
        when(jwtUtil.isExpired("valid-refresh")).thenReturn(false);
        when(jwtUtil.getUserId("valid-refresh")).thenReturn(1L);
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(jwtUtil.generateAccessToken(1L, "星旅人")).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(1L, "星旅人")).thenReturn("new-refresh");

        LoginResponse response = authService.refresh("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    @DisplayName("刷新令牌 - Token 过期时抛出异常")
    void refresh_expired() {
        when(jwtUtil.isExpired("expired-refresh")).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh("expired-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("星轨已过期，请重新登录");
    }
}
