package com.starvocab.dto;

import java.time.LocalDateTime;

/** 单词学习轨迹条目 */
public record HistoryVO(LocalDateTime reviewedAt, Integer rating, String mode,
                        Double easeFactor, Integer intervalDays) {
}
