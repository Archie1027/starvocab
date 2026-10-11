package com.starvocab.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 更新个人资料请求
 */
@Data
public class UserProfileRequest {
    private String nickname;
    private LocalDate examDate;
    private Integer dailyGoal;
}
