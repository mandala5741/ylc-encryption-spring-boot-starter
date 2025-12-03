package com.cqcloud.platform.manager;

import com.cqcloud.platform.enums.AlgorithmType;

/**
 * 加密管理器接口
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
public interface EncryptionManager {

    /**
     * 加密数据
     * @param plaintext 明文
     * @param keyId 密钥ID
     * @return 密文
     */
    String encrypt(String plaintext, String keyId);

    /**
     * 加密数据
     * @param plaintext 明文
     * @param keyId 密钥ID
     * @param algorithm 算法类型
     * @return 密文
     */
    String encrypt(String plaintext, String keyId, AlgorithmType algorithm);

    /**
     * 解密数据
     * @param ciphertext 密文
     * @param keyId 密钥ID
     * @return 明文
     */
    String decrypt(String ciphertext, String keyId);

    /**
     * 解密数据
     * @param ciphertext 密文
     * @param keyId 密钥ID
     * @param algorithm 算法类型
     * @return 明文
     */
    String decrypt(String ciphertext, String keyId, AlgorithmType algorithm);

    /**
     * 检查是否已加密
     */
    boolean isEncrypted(String text);

    /**
     * 获取默认密钥ID
     */
    String getDefaultKeyId();
}