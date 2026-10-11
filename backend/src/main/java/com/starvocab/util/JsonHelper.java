package com.starvocab.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/** definition/options JSON 解析工具 */
public final class JsonHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> DEF_TYPE =
            new TypeReference<List<Map<String, Object>>>() {};
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<List<String>>() {};

    private JsonHelper() {}

    /** [{"pos":"n.","meanings":["丰富"]}] → List<Map>；解析失败返回空列表 */
    public static List<Map<String, Object>> parseDefinition(String json) {
        try {
            return MAPPER.readValue(json == null ? "[]" : json, DEF_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** 拼接为 "n. 丰富；充裕" 形式的可读释义 */
    public static String plainDefinition(String json) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> item : parseDefinition(json)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(item.get("pos")).append(" ");
            Object meanings = item.get("meanings");
            if (meanings instanceof List<?> list) {
                sb.append(String.join("；", list.stream().map(String::valueOf).toList()));
            }
        }
        return sb.toString().trim();
    }

    /** JSON 字符串数组 → List；解析失败返回空列表 */
    public static List<String> parseArray(String json) {
        try {
            return MAPPER.readValue(json == null ? "[]" : json, LIST_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
