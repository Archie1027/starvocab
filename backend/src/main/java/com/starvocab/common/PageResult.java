package com.starvocab.common;

import java.util.List;

/** 通用分页包装 */
public record PageResult<T>(List<T> list, long total, int page, int size, long totalPages) {
    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        long totalPages = size > 0 ? (total + size - 1) / size : 0;
        return new PageResult<>(list, total, page, size, totalPages);
    }
}
