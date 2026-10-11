package com.starvocab.dto;

/** 交卷结果中的错题 */
public record WrongItem(String questionId, Long wordId, String stem, String yourAnswer, String correctAnswer) {
}
