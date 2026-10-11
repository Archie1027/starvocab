package com.starvocab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starvocab.entity.StarUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface StarUserMapper extends BaseMapper<StarUser> {
}
