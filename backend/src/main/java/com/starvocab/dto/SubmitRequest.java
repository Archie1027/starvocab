package com.starvocab.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** 交卷 */
public record SubmitRequest(@NotEmpty List<AnswerRequest> answers, Integer durationSec) {
}
