package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.dto.HistoryVO;
import com.starvocab.dto.QueueCard;
import com.starvocab.dto.ReviewRequest;
import com.starvocab.dto.ReviewResult;
import com.starvocab.dto.TodayQueue;
import com.starvocab.entity.OrbitLog;
import com.starvocab.entity.OrbitRecord;
import com.starvocab.entity.StarUser;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.OrbitLogMapper;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 学习轨道服务：今日队列 + SM-2 复习 + 学习轨迹 */
@Service
@RequiredArgsConstructor
public class OrbitService {

    private final OrbitRecordMapper orbitRecordMapper;
    private final OrbitLogMapper orbitLogMapper;
    private final StarWordMapper starWordMapper;
    private final StarUserMapper starUserMapper;
    private final Sm2Service sm2Service;

    /** 今日学习队列：到期复习卡 + 今日新词（上限 daily_goal） */
    public TodayQueue today(Long userId) {
        LocalDate today = LocalDate.now();
        StarUser user = starUserMapper.selectById(userId);
        int dailyGoal = user != null && user.getDailyGoal() != null ? user.getDailyGoal() : 50;

        List<OrbitRecord> dueRecords = orbitRecordMapper.selectList(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId)
                .le(OrbitRecord::getNextReview, today)
                .orderByAsc(OrbitRecord::getNextReview));

        List<Long> learnedIds = orbitRecordMapper.selectList(new LambdaQueryWrapper<OrbitRecord>()
                        .eq(OrbitRecord::getUserId, userId).select(OrbitRecord::getWordId))
                .stream().map(OrbitRecord::getWordId).toList();

        List<StarWord> newWords = new ArrayList<>();
        if (learnedIds.isEmpty()) {
            newWords = starWordMapper.selectList(new LambdaQueryWrapper<StarWord>()
                    .orderByDesc(StarWord::getFrequency).last("LIMIT " + dailyGoal));
        } else if (learnedIds.size() < 100000) {
            newWords = starWordMapper.selectList(new LambdaQueryWrapper<StarWord>()
                    .notIn(StarWord::getId, learnedIds)
                    .orderByDesc(StarWord::getFrequency).last("LIMIT " + dailyGoal));
        }

        List<QueueCard> queue = new ArrayList<>();
        for (OrbitRecord record : dueRecords) {
            StarWord word = starWordMapper.selectById(record.getWordId());
            if (word == null) continue;
            queue.add(new QueueCard(word.getId(), word.getWord(), word.getPhoneticUs(),
                    JsonHelper.plainDefinition(word.getDefinition()), "review",
                    record.getEaseFactor() == null ? 2.5 : record.getEaseFactor().doubleValue(),
                    record.getIntervalDays(), record.getRepetitions()));
        }
        for (StarWord word : newWords) {
            queue.add(new QueueCard(word.getId(), word.getWord(), word.getPhoneticUs(),
                    JsonHelper.plainDefinition(word.getDefinition()), "new",
                    2.5, 0, 0));
        }
        return new TodayQueue(today.format(DateTimeFormatter.ISO_DATE), newWords.size(),
                dueRecords.size(), queue);
    }

    /** 提交单卡学习结果（首次学习自动建轨道） */
    @Transactional
    public ReviewResult review(Long userId, ReviewRequest request) {
        StarWord word = starWordMapper.selectById(request.wordId());
        if (word == null) {
            throw new IllegalArgumentException("未找到该词汇，这片星域尚无星辰");
        }
        OrbitRecord record = orbitRecordMapper.selectOne(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId).eq(OrbitRecord::getWordId, request.wordId()));
        boolean isNew = record == null;
        if (isNew) {
            record = new OrbitRecord();
            record.setUserId(userId);
            record.setWordId(request.wordId());
            record.setRepetitions(0);
            record.setEaseFactor(new BigDecimal("2.50"));
            record.setIntervalDays(0);
            record.setStatus(1);
            record.setTotalReviews(0);
            record.setCorrectCount(0);
        }
        Sm2Service.Sm2Result result = sm2Service.apply(
                record.getRepetitions() == null ? 0 : record.getRepetitions(),
                record.getEaseFactor() == null ? 2.5 : record.getEaseFactor().doubleValue(),
                record.getIntervalDays() == null ? 0 : record.getIntervalDays(),
                request.rating());
        record.setRepetitions(result.repetitions());
        record.setEaseFactor(BigDecimal.valueOf(result.easeFactor()));
        record.setIntervalDays(result.intervalDays());
        record.setNextReview(result.nextReview());
        record.setLastReview(LocalDateTime.now());
        record.setTotalReviews(record.getTotalReviews() + 1);
        boolean correct = request.rating() >= 2;
        if (correct) record.setCorrectCount(record.getCorrectCount() + 1);
        record.setStatus(result.repetitions() >= 5 && request.rating() == 3 ? 2 : 1);
        if (isNew) orbitRecordMapper.insert(record);
        else orbitRecordMapper.updateById(record);

        OrbitLog log = new OrbitLog();
        log.setUserId(userId);
        log.setWordId(request.wordId());
        log.setRating(request.rating());
        log.setMode(request.mode() == null ? "flashcard" : request.mode());
        log.setEaseFactor(record.getEaseFactor());
        log.setIntervalDays(result.intervalDays());
        log.setCorrect(correct ? 1 : 0);
        log.setReviewedAt(LocalDateTime.now());
        orbitLogMapper.insert(log);

        String message = request.rating() >= 3 ? "星辰已点亮，明日再来巩固" : "这颗星先归队，明天再试一次";
        return new ReviewResult(word.getId(), record.getStatus(), record.getRepetitions(),
                record.getEaseFactor().doubleValue(), record.getIntervalDays(), record.getNextReview(), message);
    }

    /** 批量提交 */
    @Transactional
    public List<ReviewResult> batch(Long userId, List<ReviewRequest> items) {
        List<ReviewResult> results = new ArrayList<>();
        for (ReviewRequest item : items) {
            results.add(review(userId, item));
        }
        return results;
    }

    /** 单词学习轨迹 */
    public List<HistoryVO> history(Long userId, Long wordId) {
        return orbitLogMapper.selectList(new LambdaQueryWrapper<OrbitLog>()
                        .eq(OrbitLog::getUserId, userId).eq(OrbitLog::getWordId, wordId)
                        .orderByDesc(OrbitLog::getReviewedAt))
                .stream()
                .map(l -> new HistoryVO(l.getReviewedAt(), l.getRating(), l.getMode(),
                        l.getEaseFactor() == null ? null : l.getEaseFactor().doubleValue(),
                        l.getIntervalDays()))
                .toList();
    }

    /** 重置单词学习状态（删除轨道记录，重新成为新词） */
    @Transactional
    public void reset(Long userId, Long wordId) {
        int deleted = orbitRecordMapper.delete(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId).eq(OrbitRecord::getWordId, wordId));
        if (deleted == 0) {
            throw new IllegalArgumentException("这颗星辰还没有轨道记录");
        }
    }
}
