package com.cqcloud.platform.config;

import com.cqcloud.platform.enums.AlgorithmType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * 加密配置属性
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Data
@ConfigurationProperties(prefix = "spring.encryption")
public class EncryptionProperties {

    /**
     * 是否启用加密功能
     */
    private boolean enabled = true;

    /**
     * 默认加密算法
     */
    private AlgorithmType defaultAlgorithm = AlgorithmType.AES_GCM;

    /**
     * 默认密钥（Base64编码）
     */
    private String defaultKey;

    /**
     * 密钥配置映射
     */
    private Set<KeyConfig> keys = new HashSet<>();

    /**
     * 是否在启动时生成默认密钥（仅测试环境使用）
     */
    private boolean generateKeyOnStartup = false;

    /**
     * 需要加密的字段模式（支持通配符）
     */
    private Set<String> includePatterns = new HashSet<>();

    /**
     * 排除的字段模式
     */
    private Set<String> excludePatterns = new HashSet<>();

    /**
     * 是否启用MyBatis-Plus自动加解密
     */
    private boolean mybatisPlusEnabled = true;

    /**
     * 是否打印加解密日志
     */
    private boolean logEnabled = false;

    /**
     * 是否缓存加密结果
     */
    private boolean cacheEnabled = false;

    /**
     * 缓存最大大小
     */
    private int cacheMaxSize = 1000;

    /**
     * 缓存过期时间（秒）
     */
    private long cacheExpireSeconds = 300;

    /**
     * 密钥配置类
     */
    @Data
    public static class KeyConfig {
        /**
         * 密钥ID
         */
        private String id = "default";

        /**
         * 密钥值（Base64编码）
         */
        private String value;

        /**
         * 密钥算法
         */
        private AlgorithmType algorithm = AlgorithmType.AES_GCM;

        /**
         * 密钥描述
         */
        private String description;
    }
}