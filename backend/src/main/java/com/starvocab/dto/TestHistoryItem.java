package com.starvocab.dto;

import java.time.LocalDateTime;

/** 测试历史条目 */
public record TestHistoryItem(String sessionId, String type, Integer total, Integer correct,
                              Integer score, Integer durationSec, LocalDateTime finishedAt) {
}
