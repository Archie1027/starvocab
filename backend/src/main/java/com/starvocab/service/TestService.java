package com.starvocab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starvocab.common.PageResult;
import com.starvocab.dto.*;
import com.starvocab.entity.OrbitRecord;
import com.starvocab.entity.StarWord;
import com.starvocab.entity.TestQuestion;
import com.starvocab.entity.TestSession;
import com.starvocab.mapper.OrbitRecordMapper;
import com.starvocab.mapper.StarWordMapper;
import com.starvocab.mapper.TestQuestionMapper;
import com.starvocab.mapper.TestSessionMapper;
import com.starvocab.util.JsonHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 测试服务：创建会话（答案仅后端保管）→ 判分 → 交卷 */
@Service
@RequiredArgsConstructor
public class TestService {

    private final TestSessionMapper testSessionMapper;
    private final TestQuestionMapper testQuestionMapper;
    private final StarWordMapper starWordMapper;
    private final OrbitRecordMapper orbitRecordMapper;

    @Transactional
    public TestSessionVO create(Long userId, CreateTestRequest request) {
        String type = request.type();
        if (!List.of("quiz", "exam", "quick").contains(type)) {
            throw new IllegalArgumentException("未知的测试类型");
        }
        int count = request.count() != null && request.count() > 0 ? request.count()
                : (request.wordIds() != null && !request.wordIds().isEmpty() ? request.wordIds().size() : 10);
        count = Math.min(count, 100);

        List<StarWord> words = pickWords(userId, request.wordIds(), count);
        List<String> types = request.questionTypes() == null || request.questionTypes().isEmpty()
                ? List.of("en2cn", "cn2en", "spelling") : request.questionTypes();

        TestSession session = new TestSession();
        session.setUserId(userId);
        session.setType(type);
        session.setTotal(words.size());
        session.setCorrect(0);
        session.setScore(0);
        session.setStartedAt(LocalDateTime.now());
        session.setDurationSec(0);
        testSessionMapper.insert(session);

        List<TestQuestion> questions = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            questions.add(buildQuestion(session.getId(), words.get(i), types.get(i % types.size()), words, i + 1));
        }
        for (TestQuestion question : questions) {
            testQuestionMapper.insert(question);
        }

        String sessionId = "T-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%04d", session.getId());
        List<Map<String, Object>> questionVos = new ArrayList<>();
        for (TestQuestion question : questions) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("questionId", "Q" + question.getSortNo());
            vo.put("type", question.getType());
            vo.put("wordId", question.getWordId());
            vo.put("stem", question.getStem());
            vo.put("options", question.getOptions() == null ? List.of() : JsonHelper.parseArray(question.getOptions()));
            questionVos.add(vo);
        }
        return new TestSessionVO(sessionId, questionVos, words.size(), "exam".equals(type) ? words.size() * 60 : 0);
    }

    /** 选题：wordIds 优先，否则按用户学习记录随机，不足补全库 */
    private List<StarWord> pickWords(Long userId, List<Long> wordIds, int count) {
        if (wordIds != null && !wordIds.isEmpty()) {
            List<StarWord> picked = starWordMapper.selectBatchIds(wordIds.stream().limit(count).toList());
            if (picked.isEmpty()) {
                throw new IllegalArgumentException("没有可用的星辰来组卷");
            }
            return picked;
        }
        List<Long> learnedIds = orbitRecordMapper.selectList(new LambdaQueryWrapper<OrbitRecord>()
                        .eq(OrbitRecord::getUserId, userId).select(OrbitRecord::getWordId))
                .stream().map(OrbitRecord::getWordId).toList();
        Collections.shuffle(learnedIds);
        List<StarWord> picked = learnedIds.isEmpty() ? new ArrayList<>()
                : new ArrayList<>(starWordMapper.selectBatchIds(learnedIds.subList(0, Math.min(count, learnedIds.size()))));
        if (picked.size() < count) {
            List<StarWord> all = starWordMapper.selectList(null);
            all.removeAll(picked);
            Collections.shuffle(all);
            picked.addAll(all.subList(0, Math.min(count - picked.size(), all.size())));
        }
        if (picked.isEmpty()) {
            throw new IllegalArgumentException("没有可用的星辰来组卷");
        }
        return picked;
    }

    private TestQuestion buildQuestion(Long sessionId, StarWord word, String type,
                                       List<StarWord> pool, int sortNo) {
        TestQuestion question = new TestQuestion();
        question.setSessionId(sessionId);
        question.setWordId(word.getId());
        question.setType(type);
        question.setSortNo(sortNo);
        String plain = JsonHelper.plainDefinition(word.getDefinition());
        String answer;
        String stem;
        switch (type) {
            case "cn2en" -> {
                stem = plain;
                answer = word.getWord();
                question.setOptions(JsonHelper.toJson(buildOptions(word.getWord(), pool, true)));
            }
            case "spelling" -> {
                stem = plain;
                answer = word.getWord();
                question.setOptions(null);
            }
            default -> { // en2cn
                stem = word.getWord();
                answer = plain;
                question.setOptions(JsonHelper.toJson(buildOptions(plain, pool, false)));
            }
        }
        question.setStem(stem);
        question.setAnswer(answer);
        question.setExplanation(word.getWord() + " " + plain);
        return question;
    }

    /** 生成 4 选 1 选项：正确项 + 3 个干扰项 */
    private List<String> buildOptions(String correct, List<StarWord> pool, boolean byWord) {
        List<String> options = new ArrayList<>();
        options.add(correct);
        List<StarWord> shuffled = new ArrayList<>(pool);
        shuffled.sort(Comparator.comparingInt(a -> ThreadLocalRandom.current().nextInt()));
        for (StarWord other : shuffled) {
            if (options.size() >= 4) break;
            String candidate = byWord ? other.getWord() : JsonHelper.plainDefinition(other.getDefinition());
            if (!options.contains(candidate)) options.add(candidate);
        }
        Collections.shuffle(options);
        return options;
    }

    @Transactional
    public AnswerResult answer(Long userId, Long sessionId, AnswerRequest request) {
        TestQuestion question = mustFindQuestion(userId, sessionId, request.questionId());
        String expected = question.getAnswer() == null ? "" : question.getAnswer().trim();
        String given = request.answer() == null ? "" : request.answer().trim();
        boolean correct = !given.isEmpty() && expected.equalsIgnoreCase(given);
        question.setUserAnswer(given);
        question.setCorrect(correct ? 1 : 0);
        question.setCostSec(request.costSec() == null ? 0 : request.costSec());
        testQuestionMapper.updateById(question);
        return new AnswerResult(request.questionId(), correct, question.getAnswer(),
                question.getWordId(), question.getExplanation());
    }

    @Transactional
    public SubmitResult submit(Long userId, Long sessionId, SubmitRequest request) {
        TestSession session = testSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("星图会话不存在或不属于你");
        }
        List<TestQuestion> questions = testQuestionMapper.selectList(new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getSessionId, sessionId).orderByAsc(TestQuestion::getSortNo));
        Map<String, AnswerRequest> byId = new HashMap<>();
        for (AnswerRequest a : request.answers()) byId.put(a.questionId(), a);
        int correctCount = 0;
        List<WrongItem> wrong = new ArrayList<>();
        for (TestQuestion question : questions) {
            AnswerRequest given = byId.get("Q" + question.getSortNo());
            boolean correct;
            if (given != null && given.answer() != null && !given.answer().trim().isEmpty()) {
                correct = question.getAnswer().trim().equalsIgnoreCase(given.answer().trim());
                question.setUserAnswer(given.answer().trim());
            } else {
                correct = false;
                question.setUserAnswer(question.getUserAnswer() == null ? "" : question.getUserAnswer());
            }
            question.setCorrect(correct ? 1 : 0);
            if (given != null && given.costSec() != null) question.setCostSec(given.costSec());
            testQuestionMapper.updateById(question);
            if (correct) correctCount++;
            else wrong.add(new WrongItem("Q" + question.getSortNo(), question.getWordId(),
                    question.getStem(), question.getUserAnswer(), question.getAnswer()));
        }
        int score = questions.isEmpty() ? 0 : (int) Math.round(correctCount * 100.0 / questions.size());
        session.setCorrect(correctCount);
        session.setScore(score);
        session.setFinishedAt(LocalDateTime.now());
        session.setDurationSec(request.durationSec() == null ? 0 : request.durationSec());
        testSessionMapper.updateById(session);
        String sessionIdText = "T-" + session.getStartedAt().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%04d", session.getId());
        return new SubmitResult(sessionIdText, questions.size(), correctCount, score, wrong,
                session.getDurationSec(), "星图测绘完成，得分 " + score);
    }

    public PageResult<TestHistoryItem> history(Long userId, int page, int size) {
        LambdaQueryWrapper<TestSession> wrapper = new LambdaQueryWrapper<TestSession>()
                .eq(TestSession::getUserId, userId).orderByDesc(TestSession::getStartedAt);
        long total = testSessionMapper.selectCount(wrapper);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        List<TestHistoryItem> list = testSessionMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(safePage, safeSize), wrapper)
                .getRecords().stream()
                .map(s -> new TestHistoryItem("T-" + s.getStartedAt().toLocalDate()
                                .format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", s.getId()),
                        s.getType(), s.getTotal(), s.getCorrect(), s.getScore(),
                        s.getDurationSec(), s.getFinishedAt()))
                .toList();
        return PageResult.of(list, total, safePage, safeSize);
    }

    private TestQuestion mustFindQuestion(Long userId, Long sessionId, String questionId) {
        TestSession session = testSessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("星图会话不存在或不属于你");
        }
        int sortNo;
        try {
            sortNo = Integer.parseInt(questionId.replaceFirst("^Q", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("题目编号格式不正确");
        }
        TestQuestion question = testQuestionMapper.selectOne(new LambdaQueryWrapper<TestQuestion>()
                .eq(TestQuestion::getSessionId, sessionId).eq(TestQuestion::getSortNo, sortNo));
        if (question == null) throw new IllegalArgumentException("未找到该题目");
        return question;
    }
}
