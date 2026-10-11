package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.dto.*;
import com.starvocab.entity.OrbitLog;
import com.starvocab.entity.OrbitRecord;
import com.starvocab.entity.StarUser;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.OrbitLogMapper;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.mapper.StarWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** 统计服务（星云）：概览 / 学习日历 / 进度星图 / 准确率 */
@Service
@RequiredArgsConstructor
public class NebulaService {

    private final OrbitRecordMapper orbitRecordMapper;
    private final OrbitLogMapper orbitLogMapper;
    private final StarWordMapper starWordMapper;
    private final StarUserMapper starUserMapper;

    public NebulaOverview overview(Long userId) {
        LocalDate today = LocalDate.now();
        long totalLearned = orbitRecordMapper.selectCount(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId));
        long totalMastered = orbitRecordMapper.selectCount(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId).eq(OrbitRecord::getStatus, 2));
        long totalReview = orbitRecordMapper.selectCount(new LambdaQueryWrapper<OrbitRecord>()
                .eq(OrbitRecord::getUserId, userId).le(OrbitRecord::getNextReview, today));
        long totalWords = starWordMapper.selectCount(null);

        LocalDateTime dayStart = today.atStartOfDay();
        long todayLearned = orbitLogMapper.selectCount(new LambdaQueryWrapper<OrbitLog>()
                .eq(OrbitLog::getUserId, userId).eq(OrbitLog::getMode, "learn")
                .ge(OrbitLog::getReviewedAt, dayStart));
        long todayReviewed = orbitLogMapper.selectCount(new LambdaQueryWrapper<OrbitLog>()
                .eq(OrbitLog::getUserId, userId).eq(OrbitLog::getMode, "review")
                .ge(OrbitLog::getReviewedAt, dayStart));

        LocalDateTime weekAgo = dayStart.minusDays(6);
        List<OrbitLog> weekLogs = orbitLogMapper.selectList(new LambdaQueryWrapper<OrbitLog>()
                .eq(OrbitLog::getUserId, userId).ge(OrbitLog::getReviewedAt, weekAgo));
        Double accuracy7d = weekLogs.isEmpty() ? null : Math.round(
                weekLogs.stream().filter(l -> l.getCorrect() != null && l.getCorrect() == 1).count()
                        * 1000.0 / weekLogs.size()) / 1000.0;

        StarUser user = starUserMapper.selectById(userId);
        LocalDate examDate = user != null && user.getExamDate() != null ? user.getExamDate() : today;
        long daysToExam = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(today, examDate));

        return new NebulaOverview(totalLearned, totalMastered, totalReview,
                Math.max(0, totalWords - totalLearned),
                streak(userId), todayLearned, todayReviewed, accuracy7d, daysToExam);
    }

    /** 连续学习天数：从今天（或昨天）向前数有学习记录的连续天数 */
    int streak(Long userId) {
        Set<LocalDate> days = orbitLogMapper.selectList(new LambdaQueryWrapper<OrbitLog>()
                        .eq(OrbitLog::getUserId, userId)
                        .select(OrbitLog::getReviewedAt)
                        .orderByDesc(OrbitLog::getReviewedAt)
                        .last("LIMIT 400"))
                .stream()
                .map(l -> l.getReviewedAt().toLocalDate())
                .collect(Collectors.toSet());
        if (days.isEmpty()) return 0;
        LocalDate cursor = days.contains(LocalDate.now()) ? LocalDate.now() : LocalDate.now().minusDays(1);
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public CalendarData calendar(Long userId, Integer year) {
        int y = year == null ? Year.now().getValue() : year;
        LocalDate start = LocalDate.of(y, 1, 1);
        LocalDate end = LocalDate.of(y, 12, 31);
        Map<String, Long> countByDate = orbitLogMapper.selectList(new LambdaQueryWrapper<OrbitLog>()
                        .eq(OrbitLog::getUserId, userId)
                        .ge(OrbitLog::getReviewedAt, start.atStartOfDay())
                        .le(OrbitLog::getReviewedAt, end.atTime(23, 59, 59)))
                .stream()
                .collect(Collectors.groupingBy(
                        l -> l.getReviewedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE),
                        Collectors.counting()));
        List<CalendarDay> days = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long count = countByDate.getOrDefault(d.format(DateTimeFormatter.ISO_DATE), 0L);
            days.add(new CalendarDay(d.format(DateTimeFormatter.ISO_DATE), count, levelOf(count)));
        }
        return new CalendarData(y, days);
    }

    private int levelOf(long count) {
        if (count <= 0) return 0;
        if (count <= 10) return 1;
        if (count <= 25) return 2;
        if (count <= 45) return 3;
        return 4;
    }

    /** 进度星图：按首字母分组，已掌握的词点亮为星辰 */
    public ConstellationData constellation(Long userId) {
        List<StarWord> words = starWordMapper.selectList(new LambdaQueryWrapper<StarWord>()
                .orderByAsc(StarWord::getWord));
        Set<Long> masteredIds = orbitRecordMapper.selectList(new LambdaQueryWrapper<OrbitRecord>()
                        .eq(OrbitRecord::getUserId, userId).eq(OrbitRecord::getStatus, 2)
                        .select(OrbitRecord::getWordId))
                .stream().map(OrbitRecord::getWordId).collect(Collectors.toSet());
        Set<Long> learnedIds = orbitRecordMapper.selectList(new LambdaQueryWrapper<OrbitRecord>()
                        .eq(OrbitRecord::getUserId, userId).select(OrbitRecord::getWordId))
                .stream().map(OrbitRecord::getWordId).collect(Collectors.toSet());

        Map<String, List<StarPoint>> groups = new TreeMap<>();
        for (StarWord word : words) {
            boolean mastered = masteredIds.contains(word.getId());
            boolean learned = learnedIds.contains(word.getId());
            if (!learned && !mastered) continue;
            String letter = word.getWord().substring(0, 1).toUpperCase();
            groups.computeIfAbsent(letter, k -> new ArrayList<>())
                    .add(new StarPoint(word.getId(), word.getWord(),
                            word.getDifficulty() == null ? 3 : word.getDifficulty(), mastered));
        }
        List<ConstellationGroup> groupList = groups.entrySet().stream()
                .map(e -> new ConstellationGroup(e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().filter(StarPoint::mastered).count(),
                        e.getValue()))
                .toList();
        long lit = groupList.stream().mapToLong(ConstellationGroup::mastered).sum();
        return new ConstellationData(groupList, words.size(), lit);
    }

    public AccuracyData accuracy(Long userId, int days) {
        int safeDays = Math.min(Math.max(days, 7), 365);
        LocalDate start = LocalDate.now().minusDays(safeDays - 1L);
        Map<String, List<OrbitLog>> byDate = orbitLogMapper.selectList(new LambdaQueryWrapper<OrbitLog>()
                        .eq(OrbitLog::getUserId, userId)
                        .ge(OrbitLog::getReviewedAt, start.atStartOfDay()))
                .stream()
                .collect(Collectors.groupingBy(l -> l.getReviewedAt().toLocalDate()
                        .format(DateTimeFormatter.ISO_DATE)));
        List<AccuracyPoint> points = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            String key = d.format(DateTimeFormatter.ISO_DATE);
            List<OrbitLog> logs = byDate.getOrDefault(key, List.of());
            if (logs.isEmpty()) {
                points.add(new AccuracyPoint(key, null, 0));
            } else {
                long correct = logs.stream().filter(l -> l.getCorrect() != null && l.getCorrect() == 1).count();
                points.add(new AccuracyPoint(key, Math.round(correct * 1000.0 / logs.size()) / 1000.0,
                        logs.size()));
            }
        }
        return new AccuracyData(points);
    }
}
