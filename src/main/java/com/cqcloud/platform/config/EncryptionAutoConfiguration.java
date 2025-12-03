package com.cqcloud.platform.config;

import com.cqcloud.platform.manager.EncryptionManager;
import com.cqcloud.platform.manager.impl.AesEncryptionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 加密自动配置类
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(EncryptionProperties.class)
@ConditionalOnProperty(prefix = "spring.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EncryptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EncryptionManager encryptionManager(EncryptionProperties properties) {
        log.info("初始化加密管理器，默认算法: {}", properties.getDefaultAlgorithm());
        return new AesEncryptionManager(properties);
    }
}