package com.starvocab.service;

import com.starvocab.dto.UpdatePasswordRequest;
import com.starvocab.dto.UserProfileRequest;
import com.starvocab.entity.StarUser;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取个人资料
     */
    StarUser getProfile(Long userId);

    /**
     * 更新个人资料
     */
    StarUser updateProfile(Long userId, UserProfileRequest request);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, UpdatePasswordRequest request);

    /**
     * 获取学习偏好
     */
    com.starvocab.dto.PreferencesDTO getPreferences(Long userId);

    /**
     * 更新学习偏好
     */
    com.starvocab.dto.PreferencesDTO updatePreferences(Long userId, com.starvocab.dto.PreferencesDTO request);
}
