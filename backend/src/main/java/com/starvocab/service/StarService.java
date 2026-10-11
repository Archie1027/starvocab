package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.common.PageResult;
import com.starvocab.dto.StarDetail;
import com.starvocab.dto.StarItem;
import com.starvocab.entity.OrbitRecord;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/** 词库服务（浏览 / 搜索 / 详情） */
@Service
@RequiredArgsConstructor
public class StarService {

    private final StarWordMapper starWordMapper;
    private final OrbitRecordMapper orbitRecordMapper;

    public PageResult<StarItem> list(int page, int size, String letter, Integer frequencyMin,
                                     Integer frequencyMax, String pos, String root, String sort) {
        LambdaQueryWrapper<StarWord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(letter)) {
            wrapper.likeRight(StarWord::getWord, letter.toLowerCase());
        }
        if (frequencyMin != null) wrapper.ge(StarWord::getFrequency, frequencyMin);
        if (frequencyMax != null) wrapper.le(StarWord::getFrequency, frequencyMax);
        if (StringUtils.hasText(pos)) {
            wrapper.apply("definition LIKE {0}", "%\\\"pos\\\":\\\"" + pos + "\\\"%");
        }
        if (StringUtils.hasText(root)) wrapper.like(StarWord::getRoot, root);
        if ("alpha".equals(sort)) wrapper.orderByAsc(StarWord::getWord);
        else if ("difficulty".equals(sort)) wrapper.orderByAsc(StarWord::getDifficulty);
        else wrapper.orderByDesc(StarWord::getFrequency);
        long total = starWordMapper.selectCount(wrapper);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        List<StarItem> list = starWordMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(safePage, safeSize), wrapper)
                .getRecords().stream().map(this::toItem).toList();
        return PageResult.of(list, total, safePage, safeSize);
    }

    public List<StarItem> search(String q, Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        LambdaQueryWrapper<StarWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(StarWord::getWord, q).or().like(StarWord::getDefinition, q));
        wrapper.last("LIMIT " + safeLimit);
        return starWordMapper.selectList(wrapper).stream().map(this::toItem).toList();
    }

    public StarDetail detail(Long userId, Long id) {
        StarWord word = starWordMapper.selectById(id);
        if (word == null) {
            throw new IllegalArgumentException("未找到该词汇，这片星域尚无星辰");
        }
        Integer userStatus = 0;
        OrbitRecord record = orbitRecordMapper.selectOne(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId).eq(OrbitRecord::getWordId, id));
        if (record != null) userStatus = record.getStatus();
        return new StarDetail(word.getId(), word.getWord(), word.getPhoneticUk(), word.getPhoneticUs(),
                JsonHelper.parseDefinition(word.getDefinition()), word.getExampleEn(), word.getExampleCn(),
                word.getRoot(), word.getAffix(), split(word.getSynonyms()), split(word.getAntonyms()),
                word.getFrequency(), word.getCet6Count(), word.getAudioUrl(), word.getDifficulty(),
                userStatus);
    }

    private List<String> split(String csv) {
        if (!StringUtils.hasText(csv)) return List.of();
        return List.of(csv.split(","));
    }

    public StarItem toItem(StarWord word) {
        List<Map<String, Object>> def = JsonHelper.parseDefinition(word.getDefinition());
        return new StarItem(word.getId(), word.getWord(), word.getPhoneticUk(), word.getPhoneticUs(),
                def, word.getFrequency(), word.getCet6Count(), word.getDifficulty());
    }
}
