package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 词库（单词即星辰） */
@Data
@TableName("star_word")
public class StarWord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;
    private String phoneticUk;
    private String phoneticUs;
    private String definition;
    private String exampleEn;
    private String exampleCn;
    private String root;
    private String affix;
    private String synonyms;
    private String antonyms;
    private Integer frequency;
    private Integer cet6Count;
    private String audioUrl;
    private Integer difficulty;
}
