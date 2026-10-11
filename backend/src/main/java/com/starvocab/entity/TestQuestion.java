package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 测试题目（正确答案仅存后端，不下发前端） */
@Data
@TableName("test_question")
public class TestQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long wordId;
    /** en2cn/cn2en/spelling */
    private String type;
    private String stem;
    private String options;
    private String answer;
    private String explanation;
    private String userAnswer;
    private Integer correct;
    private Integer costSec;
    private Integer sortNo;
}
