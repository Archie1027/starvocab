package com.starvocab.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.StarWordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 词库初始化器：star_word 表为空时，从 classpath:vocab/cet6-words.json 导入六级核心词。
 * 只在空表时导入，保证重启不重复插入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VocabInitializer implements ApplicationRunner {

    private final StarWordMapper starWordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Long count = starWordMapper.selectCount(null);
        if (count != null && count > 0) {
            log.info("✦ [StarVocab] 词库已有 {} 颗星辰，跳过导入", count);
            return;
        }
        List<Map<String, Object>> words = objectMapper.readValue(
                new ClassPathResource("vocab/cet6-words.json").getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {});
        for (Map<String, Object> w : words) {
            StarWord sw = new StarWord();
            sw.setWord((String) w.get("word"));
            sw.setPhoneticUk((String) w.get("uk"));
            sw.setPhoneticUs((String) w.get("us"));
            sw.setDefinition(toDefinitionJson(w.get("def")));
            sw.setExampleEn((String) w.get("exampleEn"));
            sw.setExampleCn((String) w.get("exampleCn"));
            sw.setRoot((String) w.get("root"));
            sw.setAffix((String) w.get("affix"));
            sw.setSynonyms((String) w.get("synonyms"));
            sw.setAntonyms((String) w.get("antonyms"));
            sw.setFrequency((Integer) w.get("frequency"));
            sw.setCet6Count((Integer) w.get("cet6Count"));
            sw.setAudioUrl("/audio/" + w.get("word") + "-us.mp3");
            sw.setDifficulty((Integer) w.get("difficulty"));
            starWordMapper.insert(sw);
        }
        log.info("✦ [StarVocab] 词库初始化完成，共点亮 {} 颗星辰", words.size());
    }

    /** def=[[pos, meanings]] → JSON [{"pos":"n.","meanings":["丰富；充裕"]}] */
    @SuppressWarnings("unchecked")
    private String toDefinitionJson(Object def) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            if (def instanceof List<?> groups) {
                for (Object g : groups) {
                    List<Object> pair = (List<Object>) g;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("pos", pair.get(0));
                    item.put("meanings", List.of(String.valueOf(pair.get(1))));
                    list.add(item);
                }
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
