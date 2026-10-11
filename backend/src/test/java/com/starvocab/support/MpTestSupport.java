package com.starvocab.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * 纯 Mockito 单测支持：为实体初始化 MyBatis-Plus 的 lambda 列缓存。
 * Service 中 LambdaQueryWrapper 的 .select(SFunction) 会立即解析列名，
 * 需要实体 TableInfo 已注册，否则抛 "can not find lambda cache"。
 */
public final class MpTestSupport {

    private MpTestSupport() {
    }

    public static void initTableInfo(Class<?>... entities) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("test");
        for (Class<?> entity : entities) {
            TableInfoHelper.initTableInfo(assistant, entity);
        }
    }
}
