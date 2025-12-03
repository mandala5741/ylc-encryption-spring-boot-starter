package com.cqcloud.platform.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;

/**
 * 密钥生成工具类
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
public class KeyGeneratorUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成AES密钥
     */
    public static String generateAesKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成AES密钥失败", e);
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }

    /**
     * 生成SM4密钥
     */
    public static String generateSm4Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("SM4", "BC");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成SM4密钥失败", e);
            throw new RuntimeException("生成SM4密钥失败", e);
        }
    }

    /**
     * 生成随机密钥对（用于演示）
     */
    public static void main(String[] args) {
        System.out.println("AES-256 密钥: " + generateAesKey(256));
        System.out.println("AES-192 密钥: " + generateAesKey(192));
        System.out.println("AES-128 密钥: " + generateAesKey(128));
        System.out.println("SM4 密钥: " + generateSm4Key());
    }
}