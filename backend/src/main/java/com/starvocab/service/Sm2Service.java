package com.starvocab.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * SM-2 间隔重复算法（参照接口文档 10.1 参考实现）
 * rating: 0=完全忘记 1=有印象但费力 2=略有迟疑 3=轻松答对
 */
@Service
public class Sm2Service {

    public record Sm2Result(int repetitions, double easeFactor, int intervalDays, LocalDate nextReview) {}

    public Sm2Result apply(int repetitions, double easeFactor, int intervalDays, int rating) {
        if (rating < 0 || rating > 3) {
            throw new IllegalArgumentException("评分超出星轨范围（0-3）");
        }
        int nextRepetitions;
        int nextInterval;
        if (rating < 3) {
            nextRepetitions = 0;
            nextInterval = 1;
        } else {
            nextRepetitions = repetitions + 1;
            if (nextRepetitions == 1) nextInterval = 1;
            else if (nextRepetitions == 2) nextInterval = 6;
            else nextInterval = (int) Math.round(intervalDays * easeFactor);
        }
        double nextEase = easeFactor + (0.1 - (3 - rating) * (0.08 + (3 - rating) * 0.02));
        if (nextEase < 1.3) nextEase = 1.3;
        nextEase = BigDecimal.valueOf(nextEase).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return new Sm2Result(nextRepetitions, nextEase, nextInterval, LocalDate.now().plusDays(nextInterval));
    }
}
