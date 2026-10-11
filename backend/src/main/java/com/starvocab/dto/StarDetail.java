package com.starvocab.dto;

import java.util.List;
import java.util.Map;

/** 单词详情 */
public record StarDetail(Long id, String word, String phoneticUk, String phoneticUs,
                         List<Map<String, Object>> definition, String exampleEn, String exampleCn,
                         String root, String affix, List<String> synonyms, List<String> antonyms,
                         Integer frequency, Integer cet6Count, String audioUrl, Integer difficulty,
                         Integer userStatus) {
}
