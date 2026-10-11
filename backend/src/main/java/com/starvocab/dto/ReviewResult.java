package com.starvocab.dto;

import java.time.LocalDate;

/** 单卡学习结果（更新后的卡片状态） */
public record ReviewResult(Long wordId, Integer status, Integer repetitions, Double easeFactor,
                           Integer intervalDays, LocalDate nextReview, String message) {
}
