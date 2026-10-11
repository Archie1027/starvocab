package com.starvocab.controller;

import com.starvocab.common.Result;
import com.starvocab.dto.*;
import com.starvocab.service.NebulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** 统计（星云）：概览 / 学习日历 / 进度星图 / 准确率 */
@RestController
@RequestMapping("/v1/nebula")
@RequiredArgsConstructor
public class NebulaController {

    private final NebulaService nebulaService;

    @GetMapping("/overview")
    public Result<NebulaOverview> overview(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(nebulaService.overview(userId));
    }

    @GetMapping("/calendar")
    public Result<CalendarData> calendar(Authentication authentication,
                                         @RequestParam(required = false) Integer year) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(nebulaService.calendar(userId, year));
    }

    @GetMapping("/constellation")
    public Result<ConstellationData> constellation(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(nebulaService.constellation(userId));
    }

    @GetMapping("/accuracy")
    public Result<AccuracyData> accuracy(Authentication authentication,
                                         @RequestParam(defaultValue = "30") int days) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(nebulaService.accuracy(userId, days));
    }
}
