package com.starvocab.dto;

/** 今日学习队列中的单词卡 */
public record QueueCard(Long wordId, String word, String phoneticUs, String definition,
                        String type, Double easeFactor, Integer intervalDays, Integer repetitions) {
}
