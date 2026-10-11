package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 词集（星集） */
@Data
@TableName("constellation_set")
public class ConstellationSet implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Integer wordCount;
    private Integer isOfficial;
    private LocalDateTime createdAt;
}
