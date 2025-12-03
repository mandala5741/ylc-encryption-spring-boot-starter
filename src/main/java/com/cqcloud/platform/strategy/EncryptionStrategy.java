package com.cqcloud.platform.strategy;

import com.cqcloud.platform.enums.AlgorithmType;

import java.util.Map;

/**
 * 加密策略接口
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
public interface EncryptionStrategy {

    /**
     * 加密数据
     * @param plaintext 明文
     * @param key 密钥
     * @param algorithm 算法类型
     * @param params 额外参数
     * @return 密文
     */
    String encrypt(String plaintext, byte[] key, AlgorithmType algorithm, Map<String, Object> params);

    /**
     * 解密数据
     * @param ciphertext 密文
     * @param key 密钥
     * @param algorithm 算法类型
     * @param params 额外参数
     * @return 明文
     */
    String decrypt(String ciphertext, byte[] key, AlgorithmType algorithm, Map<String, Object> params);

    /**
     * 是否支持该算法
     */
    boolean supports(AlgorithmType algorithm);
}