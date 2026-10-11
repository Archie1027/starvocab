package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 学习流水（每次评分一条） */
@Data
@TableName("orbit_log")
public class OrbitLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Integer rating;
    private String mode;
    private BigDecimal easeFactor;
    private Integer intervalDays;
    private Integer correct;
    private LocalDateTime reviewedAt;
}
