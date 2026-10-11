package com.starvocab.dto;

import java.util.List;

/** 测试题目（不下发标准答案） */
public record QuestionVO(String questionId, String type, Long wordId, String stem,
                         String phonetic, List<String> options, String audioUrl) {
}
