package com.starvocab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** SM-2 算法单元测试：4 档评分分支 / easeFactor 下限 / 间隔递增与重置 */
class Sm2ServiceTest {

    private final Sm2Service sm2Service = new Sm2Service();

    @Test
    @DisplayName("首次轻松答对：repetitions=1，间隔 1 天，次日复习")
    void apply_firstSuccess() {
        Sm2Service.Sm2Result r = sm2Service.apply(0, 2.5, 0, 3);
        assertThat(r.repetitions()).isEqualTo(1);
        assertThat(r.intervalDays()).isEqualTo(1);
        assertThat(r.nextReview()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(r.easeFactor()).isEqualTo(2.6);
    }

    @Test
    @DisplayName("第二次答对：间隔 6 天")
    void apply_secondSuccess() {
        Sm2Service.Sm2Result r = sm2Service.apply(1, 2.5, 1, 3);
        assertThat(r.repetitions()).isEqualTo(2);
        assertThat(r.intervalDays()).isEqualTo(6);
    }

    @Test
    @DisplayName("第三次起：间隔 = 上次间隔 × easeFactor")
    void apply_intervalMultiplied() {
        Sm2Service.Sm2Result r = sm2Service.apply(2, 2.5, 6, 3);
        assertThat(r.repetitions()).isEqualTo(3);
        assertThat(r.intervalDays()).isEqualTo(15);
    }

    @Test
    @DisplayName("评分 0 完全忘记：repetitions 归零，间隔重置 1 天")
    void apply_forgetResets() {
        Sm2Service.Sm2Result r = sm2Service.apply(4, 2.5, 20, 0);
        assertThat(r.repetitions()).isZero();
        assertThat(r.intervalDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("评分 2 略有迟疑：ease 不变，间隔重置")
    void apply_hesitate() {
        Sm2Service.Sm2Result r = sm2Service.apply(3, 2.5, 10, 2);
        assertThat(r.repetitions()).isZero();
        assertThat(r.intervalDays()).isEqualTo(1);
        assertThat(r.easeFactor()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("连续遗忘：easeFactor 触底 1.3 不再下降")
    void apply_easeFloor() {
        double ease = 2.5;
        Sm2Service.Sm2Result r = null;
        for (int i = 0; i < 10; i++) {
            r = sm2Service.apply(0, ease, 1, 0);
            ease = r.easeFactor();
        }
        assertThat(r.easeFactor()).isEqualTo(1.3);
    }

    @Test
    @DisplayName("非法评分抛出业务异常")
    void apply_invalidRating() {
        assertThatThrownBy(() -> sm2Service.apply(0, 2.5, 0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("评分");
    }
}
