package com.cqcloud.platform.strategy.impl;

import com.cqcloud.platform.enums.AlgorithmType;
import com.cqcloud.platform.exception.DecryptException;
import com.cqcloud.platform.exception.EncryptException;
import com.cqcloud.platform.strategy.EncryptionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

/**
 * SM4-GCM加密策略实现（国密认证加密）
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
public class Sm4GcmEncryptionStrategy implements EncryptionStrategy {

    private static final String ENCRYPTION_FLAG = "ENC(SM4-GCM):";
    private static final int IV_LENGTH = 12; // GCM推荐IV长度为12字节
    private static final int GCM_TAG_LENGTH = 128; // GCM标签长度128位

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public String encrypt(String plaintext, byte[] key, AlgorithmType algorithm, Map<String, Object> params) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // 检查是否已加密
            if (isEncrypted(plaintext)) {
                return plaintext;
            }

            // 生成随机IV
            byte[] iv = generateIv();

            // 创建密钥和参数
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "SM4");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(algorithm.getTransformation(), "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);

            // 执行加密
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 组合IV和密文
            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);

            // Base64编码
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);
            return ENCRYPTION_FLAG + encryptedBase64;

        } catch (Exception e) {
            log.error("SM4-GCM加密失败", e);
            throw new EncryptException("SM4-GCM加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String ciphertext, byte[] key, AlgorithmType algorithm, Map<String, Object> params) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        // 检查是否是加密格式
        if (!isEncrypted(ciphertext)) {
            return ciphertext;
        }

        try {
            // 移除标识前缀
            String encryptedBase64 = ciphertext.substring(ENCRYPTION_FLAG.length());

            // Base64解码
            byte[] encryptedData = Base64.getDecoder().decode(encryptedBase64);

            // 提取IV和密文
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedData.length - IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedData, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // 创建密钥和参数
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "SM4");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(algorithm.getTransformation(), "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

            // 执行解密
            byte[] plaintext = cipher.doFinal(encryptedBytes);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("SM4-GCM解密失败: {}", ciphertext, e);
            throw new DecryptException("SM4-GCM解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(AlgorithmType algorithm) {
        return algorithm == AlgorithmType.SM4_GCM;
    }

    /**
     * 检查是否已加密
     */
    private boolean isEncrypted(String text) {
        return text != null && text.startsWith(ENCRYPTION_FLAG);
    }

    /**
     * 生成随机IV
     */
    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}