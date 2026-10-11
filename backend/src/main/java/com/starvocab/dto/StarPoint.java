package com.starvocab.dto;

/** 进度星图中的单颗星 */
public record StarPoint(Long wordId, String word, Integer magnitude, boolean mastered) {
}
