package com.cqcloud.platform.manager.impl;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cqcloud.platform.config.EncryptionProperties;
import com.cqcloud.platform.enums.AlgorithmType;
import com.cqcloud.platform.exception.DecryptException;
import com.cqcloud.platform.exception.EncryptException;
import com.cqcloud.platform.manager.EncryptionManager;
import com.cqcloud.platform.strategy.EncryptionStrategy;
import com.cqcloud.platform.strategy.impl.AesGcmEncryptionStrategy;
import com.cqcloud.platform.strategy.impl.Sm4CbcEncryptionStrategy;
import com.cqcloud.platform.strategy.impl.Sm4GcmEncryptionStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AES加密管理器实现
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
public class AesEncryptionManager implements EncryptionManager {

    private final Map<String, byte[]> keyStore = new HashMap<>();
    private final Map<AlgorithmType, EncryptionStrategy> strategies = new HashMap<>();
    private final EncryptionProperties properties;
    private final Cache<String, String> encryptionCache;
    private final Cache<String, String> decryptionCache;

    public AesEncryptionManager(EncryptionProperties properties) {
        this.properties = properties;

        // 初始化策略
        initStrategies();

        // 初始化密钥
        initKeys();

        // 初始化缓存
        this.encryptionCache = properties.isCacheEnabled() ?
                CacheBuilder.newBuilder()
                        .maximumSize(properties.getCacheMaxSize())
                        .expireAfterWrite(properties.getCacheExpireSeconds(), TimeUnit.SECONDS)
                        .build() : null;

        this.decryptionCache = properties.isCacheEnabled() ?
                CacheBuilder.newBuilder()
                        .maximumSize(properties.getCacheMaxSize())
                        .expireAfterWrite(properties.getCacheExpireSeconds(), TimeUnit.SECONDS)
                        .build() : null;
    }

    /**
     * 初始化策略
     */
    private void initStrategies() {
        strategies.put(AlgorithmType.AES_GCM, new AesGcmEncryptionStrategy());
        strategies.put(AlgorithmType.AES_CBC, new AesGcmEncryptionStrategy());
        strategies.put(AlgorithmType.SM4_CBC, new Sm4GcmEncryptionStrategy());
        strategies.put(AlgorithmType.SM4_GCM, new Sm4CbcEncryptionStrategy());
    }

    /**
     * 初始化密钥
     */
    private void initKeys() {
        // 添加默认密钥
        if (StringUtils.isNotBlank(properties.getDefaultKey())) {
            keyStore.put("default", Base64.getDecoder().decode(properties.getDefaultKey()));
        }

        // 添加配置的密钥
        if (properties.getKeys() != null) {
            properties.getKeys().forEach(keyConfig -> {
                if (StringUtils.isNotBlank(keyConfig.getValue())) {
                    keyStore.put(keyConfig.getId(), Base64.getDecoder().decode(keyConfig.getValue()));
                }
            });
        }

        // 如果没有密钥，生成一个（仅用于测试）
        if (keyStore.isEmpty() && properties.isGenerateKeyOnStartup()) {
            try {
                byte[] generatedKey = AesGcmEncryptionStrategy.generateKey(256);
                keyStore.put("default", generatedKey);
                log.warn("自动生成默认AES密钥（仅测试环境使用）");
            } catch (Exception e) {
                log.error("生成默认密钥失败", e);
            }
        }
    }

    @Override
    public String encrypt(String plaintext, String keyId) {
        return encrypt(plaintext, keyId, properties.getDefaultAlgorithm());
    }

    @Override
    public String encrypt(String plaintext, String keyId, AlgorithmType algorithm) {
        if (StringUtils.isBlank(plaintext)) {
            return plaintext;
        }

        // 检查缓存
        String cacheKey = buildCacheKey(plaintext, keyId, algorithm);
        if (properties.isCacheEnabled() && encryptionCache != null) {
            String cached = encryptionCache.getIfPresent(cacheKey);
            if (cached != null) {
                log.debug("命中加密缓存: {}", cacheKey);
                return cached;
            }
        }

        try {
            // 获取密钥
            byte[] key = getKey(keyId);
            if (key == null) {
                throw new EncryptException("未找到密钥: " + keyId);
            }

            // 获取策略
            EncryptionStrategy strategy = strategies.get(algorithm);
            if (strategy == null) {
                throw new EncryptException("不支持的算法: " + algorithm);
            }

            // 执行加密
            Map<String, Object> params = new HashMap<>();
            params.put("keyId", keyId);
            params.put("algorithm", algorithm);

            String ciphertext = strategy.encrypt(plaintext, key, algorithm, params);

            // 放入缓存
            if (properties.isCacheEnabled() && encryptionCache != null) {
                encryptionCache.put(cacheKey, ciphertext);
            }

            // 记录日志
            if (properties.isLogEnabled()) {
                log.debug("加密成功: keyId={}, algorithm={}, plaintextLength={}",
                        keyId, algorithm, plaintext.length());
            }

            return ciphertext;

        } catch (Exception e) {
            throw new EncryptException("加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String ciphertext, String keyId) {
        return decrypt(ciphertext, keyId, properties.getDefaultAlgorithm());
    }

    @Override
    public String decrypt(String ciphertext, String keyId, AlgorithmType algorithm) {
        if (StringUtils.isBlank(ciphertext)) {
            return ciphertext;
        }

        // 检查缓存
        String cacheKey = buildCacheKey(ciphertext, keyId, algorithm);
        if (properties.isCacheEnabled() && decryptionCache != null) {
            String cached = decryptionCache.getIfPresent(cacheKey);
            if (cached != null) {
                log.debug("命中解密缓存: {}", cacheKey);
                return cached;
            }
        }

        try {
            // 获取密钥
            byte[] key = getKey(keyId);
            if (key == null) {
                throw new DecryptException("未找到密钥: " + keyId);
            }

            // 获取策略
            EncryptionStrategy strategy = strategies.get(algorithm);
            if (strategy == null) {
                throw new DecryptException("不支持的算法: " + algorithm);
            }

            // 检查是否需要解密（有些文本可能未加密）
            if (!(boolean) strategy.getClass().getMethod("isEncrypted", String.class).invoke(strategy, ciphertext)) {
                return ciphertext;
            }

            // 执行解密
            Map<String, Object> params = new HashMap<>();
            params.put("keyId", keyId);
            params.put("algorithm", algorithm);

            String plaintext = strategy.decrypt(ciphertext, key, algorithm, params);

            // 放入缓存
            if (properties.isCacheEnabled() && decryptionCache != null) {
                decryptionCache.put(cacheKey, plaintext);
            }

            // 记录日志
            if (properties.isLogEnabled()) {
                log.debug("解密成功: keyId={}, algorithm={}, ciphertextLength={}",
                        keyId, algorithm, ciphertext.length());
            }

            return plaintext;

        } catch (Exception e) {
            throw new DecryptException("解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEncrypted(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        // 检查各种加密标识
        return text.startsWith("ENC(AES-GCM):") ||
                text.startsWith("ENC(AES-CBC):") ||
                text.startsWith("ENC(SM4-CBC):") ||
                text.startsWith("ENC(SM4-GCM):");
    }

    @Override
    public String getDefaultKeyId() {
        return "default";
    }

    /**
     * 获取密钥
     */
    private byte[] getKey(String keyId) {
        byte[] key = keyStore.get(keyId);
        if (key == null && "default".equals(keyId)) {
            // 尝试使用第一个密钥作为默认密钥
            if (!keyStore.isEmpty()) {
                key = keyStore.values().iterator().next();
            }
        }
        return key;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String text, String keyId, AlgorithmType algorithm) {
        return algorithm.name() + ":" + keyId + ":" + text;
    }

    /**
     * 添加密钥
     */
    public void addKey(String keyId, byte[] key) {
        keyStore.put(keyId, key);
        log.info("添加密钥: keyId={}, keyLength={}", keyId, key.length);
    }

    /**
     * 移除密钥
     */
    public void removeKey(String keyId) {
        keyStore.remove(keyId);
        log.info("移除密钥: keyId={}", keyId);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        if (encryptionCache != null) {
            encryptionCache.invalidateAll();
        }
        if (decryptionCache != null) {
            decryptionCache.invalidateAll();
        }
        log.debug("清空加解密缓存");
    }
}
