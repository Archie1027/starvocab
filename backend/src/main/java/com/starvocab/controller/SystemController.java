package com.starvocab.controller;

import com.starvocab.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器 - 公开配置
 */
@RestController
@RequestMapping("/v1/system")
public class SystemController {

    @Value("${starvocab.theme.default}")
    private String defaultTheme;

    @Value("${starvocab.theme.accent-color}")
    private String accentColor;

    @Value("${starvocab.theme.background}")
    private String background;

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> config = new HashMap<>();
        config.put("appName", "星词 StarVocab");
        config.put("motd", "欢迎来到星词宇宙");
        Map<String, String> theme = new HashMap<>();
        theme.put("default", defaultTheme);
        theme.put("accentColor", accentColor);
        theme.put("background", background);
        config.put("theme", theme);
        config.put("version", "1.0.0");
        return Result.ok(config);
    }
}
