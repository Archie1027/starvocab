package com.starvocab.dto;

/** 学习偏好（主题 / 动态背景 / 发音口音） */
public record PreferencesDTO(String theme, Boolean motionEnabled, String accent) {
}
