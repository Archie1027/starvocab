package com.starvocab.dto;

import java.util.List;

/** 学习日历（热力图） */
public record CalendarData(int year, List<CalendarDay> days) {
}
