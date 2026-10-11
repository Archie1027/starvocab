package com.starvocab.dto;

/** 学习概览 */
public record NebulaOverview(long totalLearned, long totalMastered, long totalReview, long remainingNew,
                             int streak, long todayLearned, long todayReviewed, Double accuracy7d,
                             long daysToExam) {
}
