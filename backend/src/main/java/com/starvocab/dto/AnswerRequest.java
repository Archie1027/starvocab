package com.starvocab.dto;

import jakarta.validation.constraints.NotBlank;

/** 提交单题答案 */
public record AnswerRequest(@NotBlank String questionId, String answer, Integer costSec) {
}
