package com.cqcloud.platform.interceptor;



import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.cqcloud.platform.annotation.Encrypted;
import com.cqcloud.platform.manager.EncryptionManager;
import com.cqcloud.platform.utils.ReflectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MyBatis-Plus加密拦截器包装器
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@RequiredArgsConstructor
public class MybatisPlusInterceptorWrapper implements Interceptor {

    private final EncryptionManager encryptionManager;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        // 判断SQL类型
        SqlCommandType sqlCommandType = ms.getSqlCommandType();

        // 处理参数加密
        if (SqlCommandType.INSERT == sqlCommandType ||
                SqlCommandType.UPDATE == sqlCommandType) {
            processEncryption(parameter);
        }

        // 执行原始方法
        Object result = invocation.proceed();

        // 处理结果解密
        if (SqlCommandType.SELECT == sqlCommandType) {
            result = processDecryption(result);
        }

        return result;
    }

    /**
     * 处理加密
     */
    private void processEncryption(Object parameter) {
        if (parameter == null) {
            return;
        }

        try {
            // 处理Map参数
            if (parameter instanceof Map) {
                Map<?, ?> paramMap = (Map<?, ?>) parameter;
                for (Object value : paramMap.values()) {
                    encryptObject(value);
                }
            }
            // 处理集合参数
            else if (parameter instanceof Collection) {
                for (Object item : (Collection<?>) parameter) {
                    encryptObject(item);
                }
            }
            // 处理单个对象
            else {
                encryptObject(parameter);
            }
        } catch (Exception e) {
            log.error("加密参数失败", e);
            throw new RuntimeException("加密参数失败", e);
        }
    }

    /**
     * 加密对象字段
     */
    private void encryptObject(Object obj) throws Exception {
        if (obj == null || ReflectionUtil.isBasicType(obj.getClass())) {
            return;
        }

        List<Field> fields = ReflectionUtil.getFieldsWithAnnotation(obj.getClass(), Encrypted.class);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue != null && !encryptionManager.isEncrypted(strValue)) {
                    Encrypted encrypted = field.getAnnotation(Encrypted.class);
                    String keyId = encrypted.keyId();
                    String encryptedValue = encryptionManager.encrypt(strValue, keyId);
                    field.set(obj, encryptedValue);

                    if (log.isDebugEnabled()) {
                        log.debug("加密字段: {}.{}", obj.getClass().getSimpleName(), field.getName());
                    }
                }
            }
        }
    }

    /**
     * 处理解密
     */
    private Object processDecryption(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // 处理List结果
            if (result instanceof List) {
                List<?> list = (List<?>) result;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Object item : list) {
                        decryptObject(item);
                    }
                }
            }
            // 处理单个对象
            else {
                decryptObject(result);
            }
        } catch (Exception e) {
            log.error("解密结果失败", e);
            // 解密失败时返回原始结果
        }

        return result;
    }

    /**
     * 解密对象字段
     */
    private void decryptObject(Object obj) throws Exception {
        if (obj == null || ReflectionUtil.isBasicType(obj.getClass())) {
            return;
        }

        List<Field> fields = ReflectionUtil.getFieldsWithAnnotation(obj.getClass(), Encrypted.class);
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);

            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue != null && encryptionManager.isEncrypted(strValue)) {
                    Encrypted encrypted = field.getAnnotation(Encrypted.class);
                    String keyId = encrypted.keyId();
                    String decryptedValue = encryptionManager.decrypt(strValue, keyId);
                    field.set(obj, decryptedValue);

                    if (log.isDebugEnabled()) {
                        log.debug("解密字段: {}.{}", obj.getClass().getSimpleName(), field.getName());
                    }
                }
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return org.apache.ibatis.plugin.Plugin.wrap(target, this);
    }
}