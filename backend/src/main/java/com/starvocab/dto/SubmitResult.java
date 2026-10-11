package com.starvocab.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 交卷结果 */
public record SubmitResult(String sessionId, Integer total, Integer correct, Integer score,
                           List<WrongItem> wrongQuestions, Integer durationSec, String message) {
}
