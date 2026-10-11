package com.starvocab.dto;

import java.time.LocalDate;
import java.util.List;

/** 今日学习队列 */
public record TodayQueue(String date, int newCount, int reviewCount, List<QueueCard> queue) {
}
