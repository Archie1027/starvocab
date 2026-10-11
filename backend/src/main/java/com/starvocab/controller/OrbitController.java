package com.starvocab.controller;

import com.starvocab.common.Result;
import com.starvocab.dto.*;
import com.starvocab.service.OrbitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 学习轨道：今日队列 / 评分复习 / 学习轨迹 / 重置 */
@RestController
@RequestMapping("/v1/orbits")
@RequiredArgsConstructor
public class OrbitController {

    private final OrbitService orbitService;

    @GetMapping("/today")
    public Result<TodayQueue> today(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orbitService.today(userId));
    }

    @PostMapping("/review")
    public Result<ReviewResult> review(Authentication authentication,
                                       @Valid @RequestBody ReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orbitService.review(userId, request));
    }

    @PostMapping("/review/batch")
    public Result<Map<String, Object>> batch(Authentication authentication,
                                             @Valid @RequestBody BatchReviewRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        List<ReviewResult> results = orbitService.batch(userId, request.items());
        return Result.ok("星轨已同步", Map.of("updated", results.size(), "results", results));
    }

    @GetMapping("/{wordId}/history")
    public Result<List<HistoryVO>> history(Authentication authentication, @PathVariable Long wordId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(orbitService.history(userId, wordId));
    }

    @DeleteMapping("/{wordId}")
    public Result<Map<String, Object>> reset(Authentication authentication, @PathVariable Long wordId) {
        Long userId = (Long) authentication.getPrincipal();
        orbitService.reset(userId, wordId);
        return Result.ok("星辰已熄灭，重新踏上轨道", Map.of("wordId", wordId));
    }
}
