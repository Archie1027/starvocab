package com.starvocab.dto;

/** 单题判分结果（交卷/提交后返回正确答案与解析） */
public record AnswerResult(String questionId, boolean correct, String correctAnswer,
                           Long wordId, String explanation) {
}
