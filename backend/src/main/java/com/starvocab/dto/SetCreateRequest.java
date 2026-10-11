package com.starvocab.dto;

import jakarta.validation.constraints.NotBlank;

/** 创建词集 */
public record SetCreateRequest(@NotBlank String name, String description) {
}
