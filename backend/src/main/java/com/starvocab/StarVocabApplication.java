package com.starvocab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 星词 StarVocab 后端启动类
 * 紫色星球主题 - 每一个单词都是宇宙中的一颗星辰
 */
@SpringBootApplication
@MapperScan("com.starvocab.mapper")
public class StarVocabApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarVocabApplication.class, args);
        System.out.println("✦ 星词 StarVocab 后端已启动，欢迎来到紫色星云 ✦");
    }
}
