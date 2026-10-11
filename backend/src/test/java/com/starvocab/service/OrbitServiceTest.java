package com.starvocab.service;

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
import com.starvocab.support.MpTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/** 学习轨道服务单元测试：今日队列合并 / 首次学习 / 复习重置 / 轨迹 */
@ExtendWith(MockitoExtension.class)
class OrbitServiceTest {

    @Mock OrbitRecordMapper orbitRecordMapper;
    @Mock OrbitLogMapper orbitLogMapper;
    @Mock StarWordMapper starWordMapper;
    @Mock StarUserMapper starUserMapper;
    @Spy Sm2Service sm2Service = new Sm2Service();
    @InjectMocks OrbitService orbitService;

    @BeforeAll
    static void initMpLambdaCache() {
        MpTestSupport.initTableInfo(OrbitRecord.class, OrbitLog.class, StarWord.class, StarUser.class);
    }

    private StarWord word(long id, String text) {
        StarWord w = new StarWord();
        w.setId(id);
        w.setWord(text);
        w.setPhoneticUs("/test/");
        w.setDefinition("[{\"pos\":\"n.\",\"meanings\":[\"测试释义\"]}]");
        w.setFrequency(1000);
        return w;
    }

    @Test
    @DisplayName("今日队列：到期复习卡与未学新词合并，复习在前")
    void today_mergesReviewAndNew() {
        StarUser user = new StarUser();
        user.setDailyGoal(30);
        when(starUserMapper.selectById(1L)).thenReturn(user);

        OrbitRecord due = new OrbitRecord();
        due.setWordId(88L);
        due.setNextReview(LocalDate.now().minusDays(1));
        due.setRepetitions(2);
        due.setIntervalDays(3);
        due.setEaseFactor(new BigDecimal("2.20"));
        OrbitRecord learned = new OrbitRecord();
        learned.setWordId(88L);
        when(orbitRecordMapper.selectList(any())).thenReturn(List.of(due), List.of(learned));

        StarWord dueWord = word(88L, "diligent");
        StarWord newWord = word(42L, "abundance");
        when(starWordMapper.selectById(88L)).thenReturn(dueWord);
        when(starWordMapper.selectList(any())).thenReturn(List.of(newWord));

        TodayQueue queue = orbitService.today(1L);
        assertThat(queue.reviewCount()).isEqualTo(1);
        assertThat(queue.newCount()).isEqualTo(1);
        assertThat(queue.queue()).hasSize(2);
        assertThat(queue.queue().get(0).type()).isEqualTo("review");
        assertThat(queue.queue().get(0).word()).isEqualTo("diligent");
        assertThat(queue.queue().get(1).type()).isEqualTo("new");
    }

    @Test
    @DisplayName("今日队列：全空时返回空队列")
    void today_empty() {
        StarUser user = new StarUser();
        user.setDailyGoal(30);
        when(starUserMapper.selectById(1L)).thenReturn(user);
        when(orbitRecordMapper.selectList(any())).thenReturn(List.of(), List.of());
        when(starWordMapper.selectList(any())).thenReturn(List.of());

        TodayQueue queue = orbitService.today(1L);
        assertThat(queue.queue()).isEmpty();
        assertThat(queue.newCount()).isZero();
        assertThat(queue.reviewCount()).isZero();
    }

    @Test
    @DisplayName("首次学习（评分3）：新建轨道，repetitions=1，次日复习")
    void review_firstLearn() {
        when(starWordMapper.selectById(42L)).thenReturn(word(42L, "abundance"));
        when(orbitRecordMapper.selectOne(any())).thenReturn(null);
        when(orbitRecordMapper.insert(any(OrbitRecord.class))).thenReturn(1);
        when(orbitLogMapper.insert(any(OrbitLog.class))).thenReturn(1);

        ReviewResult result = orbitService.review(1L, new ReviewRequest(42L, 3, "learn"));
        assertThat(result.repetitions()).isEqualTo(1);
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.easeFactor()).isEqualTo(2.6);
        assertThat(result.nextReview()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(result.status()).isEqualTo(1);
    }

    @Test
    @DisplayName("复习时完全忘记：repetitions 归零，间隔重置 1 天")
    void review_forgetResets() {
        when(starWordMapper.selectById(88L)).thenReturn(word(88L, "diligent"));
        OrbitRecord record = new OrbitRecord();
        record.setId(9L);
        record.setUserId(1L);
        record.setWordId(88L);
        record.setRepetitions(3);
        record.setEaseFactor(new BigDecimal("2.50"));
        record.setIntervalDays(15);
        record.setStatus(1);
        record.setTotalReviews(3);
        record.setCorrectCount(3);
        when(orbitRecordMapper.selectOne(any())).thenReturn(record);
        when(orbitLogMapper.insert(any(OrbitLog.class))).thenReturn(1);

        ReviewResult result = orbitService.review(1L, new ReviewRequest(88L, 0, "review"));
        assertThat(result.repetitions()).isZero();
        assertThat(result.intervalDays()).isEqualTo(1);
        assertThat(result.nextReview()).isEqualTo(LocalDate.now().plusDays(1));
    }

    @Test
    @DisplayName("复习的词不存在时抛出业务异常")
    void review_wordNotFound() {
        when(starWordMapper.selectById(anyLong())).thenReturn(null);
        assertThatThrownBy(() -> orbitService.review(1L, new ReviewRequest(999L, 3, "learn")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未找到该词汇");
    }

    @Test
    @DisplayName("重置：无轨道记录时抛出业务异常")
    void reset_noRecord() {
        when(orbitRecordMapper.delete(any())).thenReturn(0);
        assertThatThrownBy(() -> orbitService.reset(1L, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("没有轨道记录");
    }

    @Test
    @DisplayName("学习轨迹按时间倒序返回")
    void history_returnsLogs() {
        OrbitLog log = new OrbitLog();
        log.setRating(3);
        log.setMode("review");
        log.setEaseFactor(new BigDecimal("2.60"));
        log.setIntervalDays(1);
        log.setReviewedAt(LocalDateTime.now());
        when(orbitLogMapper.selectList(any())).thenReturn(List.of(log));

        var list = orbitService.history(1L, 42L);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).rating()).isEqualTo(3);
        assertThat(list.get(0).easeFactor()).isEqualTo(2.6);
    }
}
