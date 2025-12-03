package com.cqcloud.platform.enums;

import lombok.Getter;

/**
 * 加密算法类型枚举
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Getter
public enum AlgorithmType {

    /**
     * AES-GCM算法，推荐使用，支持认证加密
     */
    AES_GCM("AES/GCM/NoPadding", "AES", 256, true),

    /**
     * AES-CBC算法，兼容性好
     */
    AES_CBC("AES/CBC/PKCS5Padding", "AES", 256, false),

    /**
     * SM4-CBC算法，国密算法
     */
    SM4_CBC("SM4/CBC/PKCS5Padding", "SM4", 128, false),

    /**
     * SM4-GCM算法，国密认证加密
     */
    SM4_GCM("SM4/GCM/NoPadding", "SM4", 128, true);

    private final String transformation;
    private final String algorithm;
    private final int keySize;
    private final boolean authenticated;

    AlgorithmType(String transformation, String algorithm, int keySize, boolean authenticated) {
        this.transformation = transformation;
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.authenticated = authenticated;
    }
}