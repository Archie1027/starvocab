package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 学习/复习记录（学习轨道） */
@Data
@TableName("orbit_record")
public class OrbitRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long wordId;
    private Integer repetitions;
    private BigDecimal easeFactor;
    private Integer intervalDays;
    private LocalDate nextReview;
    private LocalDateTime lastReview;
    /** 0=新词 1=学习中 2=已掌握 3=待复习 */
    private Integer status;
    private Integer totalReviews;
    private Integer correctCount;
}
