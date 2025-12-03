package com.cqcloud.platform.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import com.cqcloud.platform.manager.EncryptionManager;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis-Plus字符串加密类型处理器
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
@MappedTypes({String.class})
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.CHAR, JdbcType.LONGVARCHAR})
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    private final EncryptionManager encryptionManager;
    private final String keyId;

    public EncryptedStringTypeHandler(EncryptionManager encryptionManager, String keyId) {
        this.encryptionManager = encryptionManager;
        this.keyId = keyId;
    }

    public EncryptedStringTypeHandler(EncryptionManager encryptionManager) {
        this(encryptionManager, "default");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            // 加密参数
            String encrypted = encryptionManager.encrypt(parameter, keyId);
            ps.setString(i, encrypted);
        } catch (Exception e) {
            log.error("设置加密参数失败: index={}, parameter={}", i, parameter, e);
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decryptValue(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decryptValue(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decryptValue(value);
    }

    /**
     * 解密值
     */
    private String decryptValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            // 检查是否需要解密
            if (encryptionManager.isEncrypted(value)) {
                return encryptionManager.decrypt(value, keyId);
            }
            return value;
        } catch (Exception e) {
            log.error("解密字段值失败: value={}", value, e);
            return value;
        }
    }
}