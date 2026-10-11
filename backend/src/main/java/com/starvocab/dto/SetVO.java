package com.starvocab.dto;

/** 词集条目 */
public record SetVO(Long id, String name, String description, Integer wordCount,
                    boolean isOfficial, boolean isMine, boolean joined) {
}
