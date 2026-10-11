package com.starvocab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体（star_user 表）
 */
@Data
@TableName("star_user")
public class StarUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private String password;

    private String avatarUrl;

    private LocalDate examDate;

    private Integer dailyGoal;

    private String theme;

    private Integer motionEnabled;

    private String accent;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
