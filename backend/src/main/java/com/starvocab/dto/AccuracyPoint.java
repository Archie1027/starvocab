package com.starvocab.dto;

/** 复习准确率单日数据点 */
public record AccuracyPoint(String date, Double accuracy, long count) {
}
