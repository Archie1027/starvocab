package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.dto.AddStarsRequest;
import com.starvocab.dto.SetCreateRequest;
import com.starvocab.dto.SetVO;
import com.starvocab.dto.StarItem;
import com.starvocab.entity.ConstellationMember;
import com.starvocab.entity.ConstellationSet;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.ConstellationMemberMapper;
import com.starvocab.mapper.ConstellationSetMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 词集（星集）服务 */
@Service
@RequiredArgsConstructor
public class ConstellationService {

    private final ConstellationSetMapper setMapper;
    private final ConstellationMemberMapper memberMapper;
    private final StarWordMapper starWordMapper;

    public List<SetVO> list(Long userId, String scope) {
        LambdaQueryWrapper<ConstellationSet> wrapper = new LambdaQueryWrapper<>();
        if ("mine".equals(scope)) wrapper.eq(ConstellationSet::getUserId, userId);
        else if ("official".equals(scope)) wrapper.eq(ConstellationSet::getIsOfficial, 1);
        else wrapper.and(w -> w.eq(ConstellationSet::getUserId, userId)
                .or().eq(ConstellationSet::getIsOfficial, 1));
        wrapper.orderByDesc(ConstellationSet::getIsOfficial).orderByAsc(ConstellationSet::getId);
        return setMapper.selectList(wrapper).stream().map(s -> toVo(s, userId)).toList();
    }

    @Transactional
    public SetVO create(Long userId, SetCreateRequest request) {
        ConstellationSet set = new ConstellationSet();
        set.setUserId(userId);
        set.setName(request.name());
        set.setDescription(request.description());
        set.setWordCount(0);
        set.setIsOfficial(0);
        setMapper.insert(set);
        return toVo(set, userId);
    }

    /** 词集内单词（分页） */
    public com.starvocab.common.PageResult<StarItem> stars(Long userId, Long setId, int page, int size) {
        ConstellationSet set = mustFindSet(setId);
        if (set.getUserId() != null && !set.getUserId().equals(userId) && set.getIsOfficial() != 1) {
            throw new IllegalArgumentException("无权访问该星集");
        }
        List<Long> wordIds = memberMapper.selectList(new LambdaQueryWrapper<ConstellationMember>()
                        .eq(ConstellationMember::getSetId, setId).orderByDesc(ConstellationMember::getAddedAt))
                .stream().map(ConstellationMember::getWordId).toList();
        long total = wordIds.size();
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        int from = Math.min((safePage - 1) * safeSize, wordIds.size());
        int to = Math.min(from + safeSize, wordIds.size());
        List<StarItem> list = new ArrayList<>();
        if (from < to) {
            list = starWordMapper.selectBatchIds(wordIds.subList(from, to)).stream()
                    .map(this::toItem).toList();
        }
        return com.starvocab.common.PageResult.of(list, total, safePage, safeSize);
    }

    private StarItem toItem(StarWord word) {
        return new StarItem(word.getId(), word.getWord(), word.getPhoneticUk(), word.getPhoneticUs(),
                JsonHelper.parseDefinition(word.getDefinition()), word.getFrequency(),
                word.getCet6Count(), word.getDifficulty());
    }

    @Transactional
    public String addStars(Long userId, Long setId, AddStarsRequest request) {
        ConstellationSet set = mustFindSet(setId);
        if (set.getUserId() != null && !set.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改该星集");
        }
        int added = 0;
        int skipped = 0;
        for (Long wordId : request.wordIds()) {
            if (starWordMapper.selectById(wordId) == null) {
                skipped++;
                continue;
            }
            Long exists = memberMapper.selectCount(new LambdaQueryWrapper<ConstellationMember>()
                    .eq(ConstellationMember::getSetId, setId).eq(ConstellationMember::getWordId, wordId));
            if (exists > 0) {
                skipped++;
                continue;
            }
            ConstellationMember member = new ConstellationMember();
            member.setSetId(setId);
            member.setWordId(wordId);
            member.setAddedAt(java.time.LocalDateTime.now());
            memberMapper.insert(member);
            added++;
        }
        if (added > 0) {
            set.setWordCount((set.getWordCount() == null ? 0 : set.getWordCount()) + added);
            setMapper.updateById(set);
        }
        return added + " 颗星辰已纳入星集" + (skipped > 0 ? "，" + skipped + " 颗已在或不存在" : "");
    }

    @Transactional
    public void removeStar(Long userId, Long setId, Long wordId) {
        ConstellationSet set = mustFindSet(setId);
        if (set.getUserId() != null && !set.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改该星集");
        }
        int deleted = memberMapper.delete(new LambdaQueryWrapper<ConstellationMember>()
                .eq(ConstellationMember::getSetId, setId).eq(ConstellationMember::getWordId, wordId));
        if (deleted > 0) {
            set.setWordCount(Math.max(0, (set.getWordCount() == null ? 0 : set.getWordCount()) - deleted));
            setMapper.updateById(set);
        }
    }

    @Transactional
    public void delete(Long userId, Long setId) {
        ConstellationSet set = mustFindSet(setId);
        if (set.getIsOfficial() == 1) {
            throw new IllegalArgumentException("官方星集不可解散");
        }
        if (!set.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能解散自己的星集");
        }
        memberMapper.delete(new LambdaQueryWrapper<ConstellationMember>()
                .eq(ConstellationMember::getSetId, setId));
        setMapper.deleteById(setId);
    }

    private ConstellationSet mustFindSet(Long setId) {
        ConstellationSet set = setMapper.selectById(setId);
        if (set == null) throw new IllegalArgumentException("星集不存在，或已在星空中散去");
        return set;
    }

    private SetVO toVo(ConstellationSet set, Long userId) {
        boolean isMine = set.getUserId() != null && set.getUserId().equals(userId);
        return new SetVO(set.getId(), set.getName(), set.getDescription(), set.getWordCount(),
                set.getIsOfficial() == 1, isMine, isMine);
    }
}
