package com.starvocab.service;

import com.starvocab.dto.AccuracyData;
import com.starvocab.dto.CalendarData;
import com.starvocab.dto.ConstellationData;
import com.starvocab.dto.NebulaOverview;
import com.starvocab.entity.OrbitLog;
import com.starvocab.entity.OrbitRecord;
import com.starvocab.entity.StarUser;
import com.starvocab.entity.StarWord;
import com.starvocab.mapper.OrbitLogMapper;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarUserMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.support.MpTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 星云统计单元测试：概览汇总 / 连续天数 / 日历 / 星图 / 准确率 */
@ExtendWith(MockitoExtension.class)
class NebulaServiceTest {

    @Mock OrbitRecordMapper orbitRecordMapper;
    @Mock OrbitLogMapper orbitLogMapper;
    @Mock StarWordMapper starWordMapper;
    @Mock StarUserMapper starUserMapper;
    @InjectMocks NebulaService nebulaService;

    @BeforeAll
    static void initMpLambdaCache() {
        MpTestSupport.initTableInfo(OrbitRecord.class, OrbitLog.class, StarWord.class, StarUser.class);
    }

    private OrbitLog log(int correct, LocalDateTime at) {
        OrbitLog l = new OrbitLog();
        l.setUserId(1L);
        l.setCorrect(correct);
        l.setReviewedAt(at);
        return l;
    }

    @Test
    @DisplayName("概览：各计数汇总、剩余新词、连续天数、7 日准确率与考试倒计时")
    void overview_summary() {
        when(orbitRecordMapper.selectCount(any())).thenReturn(10L, 4L, 3L);
        when(starWordMapper.selectCount(any())).thenReturn(120L);
        when(orbitLogMapper.selectCount(any())).thenReturn(5L, 3L);
        // 第一次 selectList：近 7 天日志（3 条 2 对）；第二次：连续天数日志（今天/昨天/前天）
        when(orbitLogMapper.selectList(any())).thenReturn(
                List.of(log(1, LocalDateTime.now()), log(0, LocalDateTime.now()), log(1, LocalDateTime.now().minusDays(1))),
                List.of(log(1, LocalDateTime.now()), log(1, LocalDateTime.now().minusDays(1)),
                        log(1, LocalDateTime.now().minusDays(2))));
        StarUser user = new StarUser();
        user.setExamDate(LocalDate.now().plusDays(10));
        when(starUserMapper.selectById(1L)).thenReturn(user);

        NebulaOverview overview = nebulaService.overview(1L);
        assertThat(overview.totalLearned()).isEqualTo(10);
        assertThat(overview.totalMastered()).isEqualTo(4);
        assertThat(overview.totalReview()).isEqualTo(3);
        assertThat(overview.remainingNew()).isEqualTo(110);
        assertThat(overview.todayLearned()).isEqualTo(5);
        assertThat(overview.todayReviewed()).isEqualTo(3);
        assertThat(overview.accuracy7d()).isEqualTo(0.667);
        assertThat(overview.streak()).isEqualTo(3);
        assertThat(overview.daysToExam()).isEqualTo(10);
    }

    @Test
    @DisplayName("概览：无学习记录时准确率为 null，连续天数为 0")
    void overview_empty() {
        when(orbitRecordMapper.selectCount(any())).thenReturn(0L, 0L, 0L);
        when(starWordMapper.selectCount(any())).thenReturn(120L);
        when(orbitLogMapper.selectCount(any())).thenReturn(0L, 0L);
        when(orbitLogMapper.selectList(any())).thenReturn(List.of());
        when(starUserMapper.selectById(1L)).thenReturn(null);

        NebulaOverview overview = nebulaService.overview(1L);
        assertThat(overview.accuracy7d()).isNull();
        assertThat(overview.streak()).isZero();
        assertThat(overview.remainingNew()).isEqualTo(120);
    }

    @Test
    @DisplayName("连续天数：今天和昨天有记录记 2 天，中间断档则重新计数")
    void streak_counts() {
        when(orbitLogMapper.selectList(any())).thenReturn(List.of(
                log(1, LocalDateTime.now()), log(1, LocalDateTime.now().minusDays(1))));
        assertThat(nebulaService.streak(1L)).isEqualTo(2);

        // 昨天有但今天没有：仍从昨天起算
        when(orbitLogMapper.selectList(any())).thenReturn(List.of(log(1, LocalDateTime.now().minusDays(1))));
        assertThat(nebulaService.streak(1L)).isEqualTo(1);
    }

    @Test
    @DisplayName("日历：按日志数划分 0-4 级热力等级")
    void calendar_levels() {
        LocalDate today = LocalDate.now();
        when(orbitLogMapper.selectList(any())).thenReturn(List.of(
                log(1, today.atTime(9, 0)), log(1, today.atTime(10, 0)),
                log(0, today.minusDays(1).atTime(9, 0)),
                log(1, today.minusDays(2).atTime(9, 0))));

        CalendarData data = nebulaService.calendar(1L, today.getYear());
        assertThat(data.year()).isEqualTo(today.getYear());
        assertThat(data.days()).hasSize(today.lengthOfYear());
        var todayEntry = data.days().stream().filter(d -> d.date().equals(today.toString())).findFirst().orElseThrow();
        assertThat(todayEntry.count()).isEqualTo(2);
        assertThat(todayEntry.level()).isEqualTo(1);
        var empty = data.days().stream().filter(d -> d.count() == 0).findFirst().orElseThrow();
        assertThat(empty.level()).isZero();
    }

    @Test
    @DisplayName("进度星图：只含已学词，按首字母分组，已掌握的点亮")
    void constellation_groups() {
        StarWord a = new StarWord();
        a.setId(1L);
        a.setWord("abundance");
        a.setDifficulty(3);
        StarWord b = new StarWord();
        b.setId(2L);
        b.setWord("banana");
        StarWord c = new StarWord();
        c.setId(3L);
        c.setWord("cat");
        when(starWordMapper.selectList(any())).thenReturn(List.of(a, b, c));
        OrbitRecord mastered = new OrbitRecord();
        mastered.setWordId(1L);
        OrbitRecord learned = new OrbitRecord();
        learned.setWordId(2L);
        when(orbitRecordMapper.selectList(any())).thenReturn(List.of(mastered), List.of(mastered, learned));

        ConstellationData data = nebulaService.constellation(1L);
        assertThat(data.totalStars()).isEqualTo(3);
        assertThat(data.litStars()).isEqualTo(1);
        assertThat(data.groups()).hasSize(2);
        assertThat(data.groups().get(0).name()).isEqualTo("A");
        assertThat(data.groups().get(0).total()).isEqualTo(1);
        assertThat(data.groups().get(0).mastered()).isEqualTo(1);
        assertThat(data.groups().get(1).name()).isEqualTo("B");
        assertThat(data.groups().get(1).mastered()).isZero();
        // 未学习的 cat 不出现在星图中
        assertThat(data.groups().stream().flatMap(g -> g.stars().stream()))
                .noneMatch(p -> p.word().equals("cat"));
    }

    @Test
    @DisplayName("准确率：逐日统计，无记录日为 null，天数下限为 7")
    void accuracy_daily() {
        LocalDate today = LocalDate.now();
        when(orbitLogMapper.selectList(any())).thenReturn(List.of(
                log(1, today.atTime(9, 0)), log(0, today.atTime(10, 0))));

        AccuracyData data = nebulaService.accuracy(1L, 3);
        assertThat(data.points()).hasSize(7);
        var todayEntry = data.points().get(data.points().size() - 1);
        assertThat(todayEntry.date()).isEqualTo(today.toString());
        assertThat(todayEntry.accuracy()).isEqualTo(0.5);
        assertThat(todayEntry.count()).isEqualTo(2);
        var empty = data.points().get(0);
        assertThat(empty.accuracy()).isNull();
        assertThat(empty.count()).isZero();
    }
}
