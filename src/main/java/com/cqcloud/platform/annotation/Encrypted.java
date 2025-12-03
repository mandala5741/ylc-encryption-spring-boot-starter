package com.cqcloud.platform.annotation;

import com.cqcloud.platform.enums.AlgorithmType;

import java.lang.annotation.*;

/**
 * 字段加密注解
 * 标记需要加密的数据库字段
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypted {

    /**
     * 是否支持模糊查询（需要特殊实现）
     */
    boolean fuzzyQuery() default false;

    /**
     * 加密算法类型
     */
    AlgorithmType algorithm() default AlgorithmType.AES_GCM;

    /**
     * 密钥ID（用于多密钥场景）
     */
    String keyId() default "default";

    /**
     * 自定义字段格式（用于复杂类型加密）
     */
    String format() default "";
}