package com.starvocab.controller;

import com.starvocab.common.PageResult;
import com.starvocab.common.Result;
import com.starvocab.dto.*;
import com.starvocab.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** 测试（星图测绘）：创建会话 / 单题判分 / 交卷 / 历史 */
@RestController
@RequestMapping("/v1/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping
    public Result<TestSessionVO> create(Authentication authentication,
                                        @Valid @RequestBody CreateTestRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok("星图测绘开始", testService.create(userId, request));
    }

    @PostMapping("/{sessionId}/answer")
    public Result<AnswerResult> answer(Authentication authentication,
                                       @PathVariable String sessionId,
                                       @Valid @RequestBody AnswerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(testService.answer(userId, parseSessionId(sessionId), request));
    }

    @PostMapping("/{sessionId}/submit")
    public Result<SubmitResult> submit(Authentication authentication,
                                       @PathVariable String sessionId,
                                       @Valid @RequestBody SubmitRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(testService.submit(userId, parseSessionId(sessionId), request));
    }

    @GetMapping("/history")
    public Result<PageResult<TestHistoryItem>> history(Authentication authentication,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(testService.history(userId, page, size));
    }

    /** "T-20261011-0001" → 数据库自增 id */
    private Long parseSessionId(String sessionId) {
        try {
            return Long.parseLong(sessionId.substring(sessionId.lastIndexOf('-') + 1));
        } catch (Exception e) {
            throw new IllegalArgumentException("会话编号格式不正确");
        }
    }
}
