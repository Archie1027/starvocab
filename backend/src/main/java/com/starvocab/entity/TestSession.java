package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 测试会话 */
@Data
@TableName("test_session")
public class TestSession implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** quiz/exam/quick */
    private String type;
    private Integer total;
    private Integer correct;
    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer durationSec;
}
