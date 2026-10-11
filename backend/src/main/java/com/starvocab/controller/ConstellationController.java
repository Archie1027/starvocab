package com.starvocab.controller;

import com.starvocab.common.PageResult;
import com.starvocab.common.Result;
import com.starvocab.dto.AddStarsRequest;
import com.starvocab.dto.SetCreateRequest;
import com.starvocab.dto.SetVO;
import com.starvocab.dto.StarItem;
import com.starvocab.service.ConstellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 词集（星集）：列表 / 创建 / 词集内单词 / 添加移除 / 删除 */
@RestController
@RequestMapping("/v1/constellations")
@RequiredArgsConstructor
public class ConstellationController {

    private final ConstellationService constellationService;

    @GetMapping
    public Result<List<SetVO>> list(Authentication authentication,
                                    @RequestParam(defaultValue = "all") String scope) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(constellationService.list(userId, scope));
    }

    @PostMapping
    public Result<SetVO> create(Authentication authentication,
                                @Valid @RequestBody SetCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok("新星集已升起", constellationService.create(userId, request));
    }

    @GetMapping("/{id}/stars")
    public Result<PageResult<StarItem>> stars(Authentication authentication,
                                              @PathVariable Long id,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "50") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(constellationService.stars(userId, id, page, size));
    }

    @PostMapping("/{id}/stars")
    public Result<Map<String, Object>> addStars(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody AddStarsRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        String message = constellationService.addStars(userId, id, request);
        return Result.ok(message, Map.of("message", message));
    }

    @DeleteMapping("/{id}/stars/{wordId}")
    public Result<Map<String, Object>> removeStar(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @PathVariable Long wordId) {
        Long userId = (Long) authentication.getPrincipal();
        constellationService.removeStar(userId, id, wordId);
        return Result.ok("星辰已移出星集", Map.of("wordId", wordId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        constellationService.delete(userId, id);
        return Result.ok("星集已解散", null);
    }
}
