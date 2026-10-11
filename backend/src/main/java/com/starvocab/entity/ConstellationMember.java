package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 词集-单词关联 */
@Data
@TableName("constellation_member")
public class ConstellationMember implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long setId;
    private Long wordId;
    private LocalDateTime addedAt;
}
