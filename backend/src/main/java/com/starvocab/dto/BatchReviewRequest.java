package com.starvocab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** 批量提交学习结果 */
public record BatchReviewRequest(@NotEmpty List<@Valid ReviewRequest> items) {
}
