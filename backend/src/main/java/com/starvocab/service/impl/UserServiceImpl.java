package com.starvocab.service.impl;

import com.starvocab.dto.UpdatePasswordRequest;
import com.starvocab.dto.UserProfileRequest;
import com.starvocab.entity.StarUser;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final StarUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StarUser getProfile(Long userId) {
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星图同步失败，用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public StarUser updateProfile(Long userId, UserProfileRequest request) {
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星图同步失败，用户不存在");
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getExamDate() != null) {
            user.setExamDate(request.getExamDate());
        }
        if (request.getDailyGoal() != null) {
            user.setDailyGoal(request.getDailyGoal());
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星图同步失败，用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于 6 位");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public com.starvocab.dto.PreferencesDTO getPreferences(Long userId) {
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星图同步失败，用户不存在");
        }
        return new com.starvocab.dto.PreferencesDTO(user.getTheme(),
                user.getMotionEnabled() != null && user.getMotionEnabled() == 1, user.getAccent());
    }

    @Override
    public com.starvocab.dto.PreferencesDTO updatePreferences(Long userId, com.starvocab.dto.PreferencesDTO request) {
        StarUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("星图同步失败，用户不存在");
        }
        if (request.theme() != null) user.setTheme(request.theme());
        if (request.motionEnabled() != null) {
            user.setMotionEnabled(request.motionEnabled() ? 1 : 0);
        }
        if (request.accent() != null) user.setAccent(request.accent());
        userMapper.updateById(user);
        return getPreferences(userId);
    }
}
