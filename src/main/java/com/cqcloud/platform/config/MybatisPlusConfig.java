package com.cqcloud.platform.config;

import com.cqcloud.platform.interceptor.MybatisPlusInterceptorWrapper;
import com.cqcloud.platform.manager.EncryptionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

/**
 * MyBatis-Plus加密配置
 * @author weimeilayer@gmail.com ✨
 * @date 💓💕 2025-12-01 16:18:08 🐬🐇 💓💕
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnClass({SqlSessionFactory.class, DataSource.class})
@ConditionalOnBean({EncryptionManager.class})
@ConditionalOnProperty(prefix = "spring.encryption", name = "mybatis-plus-enabled", havingValue = "true", matchIfMissing = true)
public class MybatisPlusConfig {

    private final EncryptionManager encryptionManager;

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptorWrapper mybatisPlusEncryptionInterceptor() {
        log.info("初始化MyBatis-Plus加密拦截器");
        return new MybatisPlusInterceptorWrapper(encryptionManager);
    }

    /**
     * 向所有SqlSessionFactory注册拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public Object registerInterceptor(List<SqlSessionFactory> sqlSessionFactories) {
        if (sqlSessionFactories == null || sqlSessionFactories.isEmpty()) {
            log.warn("没有找到SqlSessionFactory，无法注册加密拦截器");
            return null;
        }

        Interceptor interceptor = mybatisPlusEncryptionInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            try {
                org.apache.ibatis.session.Configuration configuration =
                        sqlSessionFactory.getConfiguration();

                // 检查是否已经注册过该拦截器
                boolean alreadyRegistered = configuration.getInterceptors()
                        .stream()
                        .anyMatch(existing -> existing.getClass().equals(interceptor.getClass()));

                if (!alreadyRegistered) {
                    configuration.addInterceptor(interceptor);
                    log.info("成功向SqlSessionFactory注册加密拦截器: {}",
                            sqlSessionFactory.getClass().getSimpleName());
                } else {
                    log.debug("加密拦截器已注册，跳过: {}",
                            sqlSessionFactory.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("向SqlSessionFactory注册拦截器失败: {}",
                        sqlSessionFactory.getClass().getName(), e);
            }
        }

        return new Object();
    }
}