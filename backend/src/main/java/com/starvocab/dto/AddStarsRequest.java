package com.starvocab.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** 添加单词到词集 */
public record AddStarsRequest(@NotEmpty List<Long> wordIds) {
}
