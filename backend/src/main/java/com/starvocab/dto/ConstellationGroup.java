package com.starvocab.dto;

import java.util.List;

/** 进度星图分组（按首字母） */
public record ConstellationGroup(String name, long total, long mastered, List<StarPoint> stars) {
}
