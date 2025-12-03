package com.cqcloud.platform.annotation;

import java.lang.annotation.*;

/**
 * 类级别加密注解
 * 标记整个类需要加密处理
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedEncryption {

    /**
     * 是否启用类级别加密
     */
    boolean enabled() default true;
}
