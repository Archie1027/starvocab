package com.starvocab.dto;

import java.util.List;
import java.util.Map;

/** 创建测试会话响应 */
public record TestSessionVO(String sessionId, List<Map<String, Object>> questions, Integer total, Integer limitSec) {
}
