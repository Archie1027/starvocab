package com.starvocab.dto;

import java.util.List;
import java.util.Map;

/** 词库列表 / 搜索项 */
public record StarItem(Long id, String word, String phoneticUk, String phoneticUs,
                       List<Map<String, Object>> definition, Integer frequency,
                       Integer cet6Count, Integer difficulty) {
}
