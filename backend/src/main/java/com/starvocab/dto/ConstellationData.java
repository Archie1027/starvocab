package com.starvocab.dto;

import java.util.List;

/** 进度星图 */
public record ConstellationData(List<ConstellationGroup> groups, long totalStars, long litStars) {
}
