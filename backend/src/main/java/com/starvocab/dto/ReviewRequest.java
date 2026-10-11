package com.starvocab.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 提交单卡学习结果 */
public record ReviewRequest(@NotNull Long wordId,
                            @NotNull @Min(0) @Max(3) Integer rating,
                            String mode) {
}
