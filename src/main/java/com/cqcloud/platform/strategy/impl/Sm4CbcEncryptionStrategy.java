package com.cqcloud.platform.strategy.impl;

import com.cqcloud.platform.enums.AlgorithmType;
import com.cqcloud.platform.exception.DecryptException;
import com.cqcloud.platform.exception.EncryptException;
import com.cqcloud.platform.strategy.EncryptionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

/**
 * SM4-CBC加密策略实现（国密算法）
 */
@Slf4j
public class Sm4CbcEncryptionStrategy implements EncryptionStrategy {

    private static final String ENCRYPTION_FLAG = "ENC(SM4-CBC):";
    private static final int IV_LENGTH = 16; // SM4的IV长度是16字节
    private static final int KEY_SIZE = 128; // SM4密钥长度128位
    private static final String SM4_ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS5Padding";

    static {
        // 注册BouncyCastle提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
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
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, SM4_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

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
            log.error("SM4-CBC加密失败", e);
            throw new EncryptException("SM4-CBC加密失败: " + e.getMessage(), e);
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

            // 检查数据长度是否足够
            if (encryptedData.length <= IV_LENGTH) {
                throw new DecryptException("加密数据格式错误: 数据太短");
            }

            // 提取IV和密文
            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedData.length - IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedData, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // 创建密钥和参数
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, SM4_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            // 执行解密
            byte[] plaintext = cipher.doFinal(encryptedBytes);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("SM4-CBC解密失败: {}", ciphertext, e);
            throw new DecryptException("SM4-CBC解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(AlgorithmType algorithm) {
        return algorithm == AlgorithmType.SM4_CBC || algorithm == AlgorithmType.SM4_GCM;
    }

    /**
     * 检查是否已加密
     */
    public boolean isEncrypted(String text) {
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

    /**
     * 生成SM4密钥
     */
    public static byte[] generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(SM4_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            log.error("生成SM4密钥失败", e);
            throw new RuntimeException("生成SM4密钥失败", e);
        }
    }

    /**
     * 生成Base64编码的SM4密钥
     */
    public static String generateBase64Key() {
        byte[] key = generateKey();
        return Base64.getEncoder().encodeToString(key);
    }
}