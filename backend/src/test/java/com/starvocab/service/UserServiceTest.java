package com.starvocab.service;

import com.starvocab.dto.UpdatePasswordRequest;
import com.starvocab.entity.StarUser;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserService 单元测试
 * 覆盖：修改密码（原密码正确、原密码错误、新密码过短）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock
    private StarUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private StarUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new StarUser();
        mockUser.setId(1L);
        mockUser.setNickname("星旅人");
        mockUser.setPassword("$2a$10$oldEncodedHash");
    }

    @Test
    @DisplayName("修改密码 - 原密码正确且新密码合法")
    void updatePassword_success() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("123456");
        request.setNewPassword("newPass678");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass678")).thenReturn("encodedNewPass");

        userService.updatePassword(1L, request);

        verify(passwordEncoder).encode("newPass678");
        verify(userMapper).updateById(mockUser);
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void updatePassword_wrongOldPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("wrong");
        request.setNewPassword("newPass678");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", mockUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("原密码不正确");

        verify(userMapper, never()).updateById(any(StarUser.class));
    }

    @Test
    @DisplayName("修改密码 - 新密码长度不足 6 位")
    void updatePassword_tooShort() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("123456");
        request.setNewPassword("12345");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", mockUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("新密码长度不能少于 6 位");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).updateById(any(StarUser.class));
    }

    @Test
    @DisplayName("修改密码 - 用户不存在")
    void updatePassword_userNotFound() {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setOldPassword("123456");
        request.setNewPassword("newPass678");

        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userService.updatePassword(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("星图同步失败，用户不存在");
    }
}
