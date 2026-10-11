package com.starvocab.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** 创建测试会话 */
public record CreateTestRequest(@NotBlank String type,
                                List<Long> wordIds,
                                List<String> questionTypes,
                                Integer count) {
}
