package com.starvocab.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.starvocab.dto.AnswerRequest;
import com.starvocab.dto.AnswerResult;
import com.starvocab.dto.CreateTestRequest;
import com.starvocab.dto.SubmitRequest;
import com.starvocab.dto.SubmitResult;
import com.starvocab.dto.TestHistoryItem;
import com.starvocab.dto.TestSessionVO;
import com.starvocab.entity.StarWord;
import com.starvocab.entity.TestQuestion;
import com.starvocab.entity.TestSession;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.mapper.TestQuestionMapper;
import com.starvocab.mapper.TestSessionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 测试服务单元测试：组卷 / 逐题判分 / 交卷计分 / 历史 */
@ExtendWith(MockitoExtension.class)
class TestServiceTest {

    @Mock TestSessionMapper testSessionMapper;
    @Mock TestQuestionMapper testQuestionMapper;
    @Mock StarWordMapper starWordMapper;
    @Mock OrbitRecordMapper orbitRecordMapper;
    @InjectMocks TestService testService;

    private StarWord word(long id, String text) {
        StarWord w = new StarWord();
        w.setId(id);
        w.setWord(text);
        w.setDefinition("[{\"pos\":\"n.\",\"meanings\":[\"释义" + id + "\"]}]");
        return w;
    }

    private TestSession session(long id, long userId) {
        TestSession s = new TestSession();
        s.setId(id);
        s.setUserId(userId);
        s.setType("quiz");
        s.setStartedAt(LocalDateTime.now());
        return s;
    }

    @Test
    @DisplayName("创建会话：自动轮转题型，sessionId 格式正确，不下发答案")
    void create_withWordIds() {
        when(starWordMapper.selectBatchIds(anyList())).thenReturn(List.of(
                word(1L, "abundance"), word(2L, "diligent"), word(3L, "ambiguous"), word(4L, "resilient")));
        doAnswer(inv -> {
            inv.getArgument(0, TestSession.class).setId(5L);
            return 1;
        }).when(testSessionMapper).insert(any(TestSession.class));
        when(testQuestionMapper.insert(any(TestQuestion.class))).thenReturn(1);

        TestSessionVO vo = testService.create(1L, new CreateTestRequest("quiz", List.of(1L, 2L, 3L, 4L), null, null));

        assertThat(vo.sessionId()).isEqualTo("T-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-0005");
        assertThat(vo.total()).isEqualTo(4);
        assertThat(vo.questions()).hasSize(4);
        assertThat(vo.questions().get(0).get("questionId")).isEqualTo("Q1");
        assertThat(vo.questions().get(0).get("type")).isEqualTo("en2cn");
        assertThat(vo.questions().get(1).get("type")).isEqualTo("cn2en");
        assertThat(vo.questions().get(2).get("type")).isEqualTo("spelling");
        // 答案仅后端保管，不下发
        assertThat(vo.questions().get(0)).doesNotContainKey("answer");
        // en2cn 题应为 4 选 1
        assertThat((List<?>) vo.questions().get(0).get("options")).hasSize(4);
        // spelling 题无选项
        assertThat((List<?>) vo.questions().get(2).get("options")).isEmpty();
        verify(testQuestionMapper, times(4)).insert(any(TestQuestion.class));
    }

    @Test
    @DisplayName("创建会话：未知测试类型抛出业务异常")
    void create_invalidType() {
        assertThatThrownBy(() -> testService.create(1L, new CreateTestRequest("mock", null, null, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未知的测试类型");
    }

    @Test
    @DisplayName("创建会话：指定的词都不存在时抛出业务异常")
    void create_noWords() {
        when(starWordMapper.selectBatchIds(anyList())).thenReturn(List.of());
        assertThatThrownBy(() -> testService.create(1L, new CreateTestRequest("quiz", List.of(999L), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("没有可用的星辰");
    }

    @Test
    @DisplayName("逐题判分：答案不区分大小写，返回正确答案与解析")
    void answer_correct() {
        when(testSessionMapper.selectById(5L)).thenReturn(session(5L, 1L));
        TestQuestion q = new TestQuestion();
        q.setSessionId(5L);
        q.setSortNo(1);
        q.setWordId(42L);
        q.setAnswer("abundance");
        q.setStem("n. 释义42");
        q.setExplanation("abundance n. 释义42");
        when(testQuestionMapper.selectOne(any())).thenReturn(q);

        AnswerResult result = testService.answer(1L, 5L, new AnswerRequest("Q1", "  ABUNDANCE  ", 8));
        assertThat(result.correct()).isTrue();
        assertThat(result.correctAnswer()).isEqualTo("abundance");
        assertThat(result.wordId()).isEqualTo(42L);
        verify(testQuestionMapper).updateById(any(TestQuestion.class));
    }

    @Test
    @DisplayName("逐题判分：会话不属于当前用户时拒绝")
    void answer_wrongOwner() {
        when(testSessionMapper.selectById(5L)).thenReturn(session(5L, 2L));
        assertThatThrownBy(() -> testService.answer(1L, 5L, new AnswerRequest("Q1", "x", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不属于");
    }

    @Test
    @DisplayName("交卷：未逐题提交的题目也参与判分，计算得分与错题")
    void submit_scores() {
        when(testSessionMapper.selectById(5L)).thenReturn(session(5L, 1L));
        TestQuestion q1 = new TestQuestion();
        q1.setSortNo(1);
        q1.setWordId(1L);
        q1.setAnswer("abundance");
        q1.setStem("n. 释义1");
        TestQuestion q2 = new TestQuestion();
        q2.setSortNo(2);
        q2.setWordId(2L);
        q2.setAnswer("diligent");
        q2.setStem("n. 释义2");
        when(testQuestionMapper.selectList(any())).thenReturn(List.of(q1, q2));
        when(testSessionMapper.updateById(any(TestSession.class))).thenReturn(1);

        SubmitResult result = testService.submit(1L, 5L,
                new SubmitRequest(List.of(new AnswerRequest("Q1", "abundance", 5)), 30));

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.correct()).isEqualTo(1);
        assertThat(result.score()).isEqualTo(50);
        assertThat(result.wrongQuestions()).hasSize(1);
        assertThat(result.wrongQuestions().get(0).questionId()).isEqualTo("Q2");
        assertThat(result.wrongQuestions().get(0).correctAnswer()).isEqualTo("diligent");
        assertThat(result.durationSec()).isEqualTo(30);
        verify(testQuestionMapper, times(2)).updateById(any(TestQuestion.class));
    }

    @Test
    @DisplayName("交卷：会话不存在时抛出业务异常")
    void submit_sessionNotFound() {
        when(testSessionMapper.selectById(5L)).thenReturn(null);
        assertThatThrownBy(() -> testService.submit(1L, 5L,
                new SubmitRequest(List.of(new AnswerRequest("Q1", "a", 1)), 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("历史：按分页返回会话记录")
    void history_paged() {
        TestSession s = session(5L, 1L);
        s.setTotal(4);
        s.setCorrect(3);
        s.setScore(75);
        s.setDurationSec(60);
        s.setFinishedAt(LocalDateTime.now());
        when(testSessionMapper.selectCount(any())).thenReturn(1L);
        when(testSessionMapper.selectPage(any(), any())).thenAnswer(inv -> {
            Page<TestSession> page = new Page<>(1, 10);
            page.setRecords(List.of(s));
            return page;
        });

        var result = testService.history(1L, 1, 10);
        assertThat(result.total()).isEqualTo(1);
        List<TestHistoryItem> items = result.list();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).sessionId()).isEqualTo("T-"
                + s.getStartedAt().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE) + "-0005");
        assertThat(items.get(0).score()).isEqualTo(75);
    }
}
