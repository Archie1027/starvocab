package com.starvocab.controller;

import com.starvocab.common.PageResult;
import com.starvocab.common.Result;
import com.starvocab.dto.StarDetail;
import com.starvocab.dto.StarItem;
import com.starvocab.service.StarService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 词库（星辰）：浏览 / 搜索 / 详情 */
@RestController
@RequestMapping("/v1/stars")
@RequiredArgsConstructor
public class StarController {

    private final StarService starService;

    @GetMapping
    public Result<PageResult<StarItem>> list(Authentication authentication,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String letter,
                                             @RequestParam(required = false) Integer frequencyMin,
                                             @RequestParam(required = false) Integer frequencyMax,
                                             @RequestParam(required = false) String pos,
                                             @RequestParam(required = false) String root,
                                             @RequestParam(required = false) String sort) {
        return Result.ok(starService.list(page, size, letter, frequencyMin, frequencyMax, pos, root, sort));
    }

    @GetMapping("/search")
    public Result<List<StarItem>> search(@RequestParam String q,
                                         @RequestParam(required = false) Integer limit) {
        return Result.ok(starService.search(q, limit));
    }

    @GetMapping("/{id}")
    public Result<StarDetail> detail(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(starService.detail(userId, id));
    }
}
