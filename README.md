# application.yml
spring:
  encryption:
    enabled: true
    default-key: "你的Base64编码AES密钥"  //YmFzZTY0LWVuY29kZWQta2V5LWhlcmU=
    keys:
      - id: default
        value: "你的Base64编码AES密钥"
        algorithm: AES_GCM
      - id: sm4
        value: "你的Base64编码SM4密钥"
        algorithm: SM4_CBC

# 加密配置
encryption:
    enabled: true
    secret-key: "your-base64-encoded-aes-256-key-here" # 32字节Base64编码密钥
    generate-random-key-for-test: false # 测试环境下可设置为true

# 数据库配置
spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database 
      username: your_username
      password: your_password
      driver-class-name: com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis:
    configuration:
        map-underscore-to-camel-case: true
        log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

## 引入依赖
<dependency>
    <groupId>io.github.mandala5741</groupId>
    <artifactId>ylc-smart-spring-boot-starter</artifactId>
    <version>1.0.2</version>
</dependency>


## 配置密钥

yaml
spring:
  encryption:
  enabled: true
  default-key: "your-base64-aes-key-here"

## 在实体类字段上添加注解

@Encrypted
private String phone;

@Encrypted(algorithm = AlgorithmType.SM4_CBC)
private String idCard;

## 手动加解密（可选）

@Autowired
private EncryptionManager encryptionManager;

// 加密
String encrypted = encryptionManager.encrypt("plaintext", "default");

// 解密
String decrypted = encryptionManager.decrypt(encrypted, "default");

## 使用示例

package com.example.demo.entity;

import com.example.encryption.annotation.Encrypted;

public class User {
private Long id;
private String username;

    @Encrypted
    private String phone;
    
    @Encrypted
    private String email;
    
    @Encrypted(supportFuzzyQuery = true)
    private String idCard;
    
    // Getter和Setter方法
    // ...
}

package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    /**
     * 保存用户 - 自动加密敏感字段
     */
    public void saveUser(User user) {
        // 不需要手动加密，拦截器会自动处理
        userMapper.insert(user);
    }
    
    /**
     * 查询用户 - 自动解密敏感字段
     */
    public User getUserById(Long id) {
        // 查询结果会自动解密
        return userMapper.selectById(id);
    }
    
    /**
     * 手动加密（用于特殊场景）
     */
    public String manualEncrypt(String plaintext) {
        return CryptoUtil.encrypt(plaintext);
    }
    
    /**
     * 手动解密（用于特殊场景）
     */
    public String manualDecrypt(String encryptedText) {
        return CryptoUtil.decrypt(encryptedText);
    }
}


## 主要特性：
自动加解密：通过MyBatis拦截器自动处理字段加解密
安全算法：使用AES-GCM算法，提供认证加密
注解驱动：使用@Encrypted注解标记需要加密的字段
配置灵活：支持通过配置文件启用/禁用，配置密钥等
避免重复加密：自动检测已加密数据
支持批量操作：自动处理List和Map类型的参数
线程安全：使用ThreadLocal管理密钥
开箱即用：Spring Boot自动配置，无需复杂配置
支持多种算法：AES-GCM、AES-CBC、SM4-GCM、SM4-CBC
MyBatis-Plus集成：自动拦截SQL进行字段加解密
注解驱动：使用@Encrypted注解标记需要加密的字段
多密钥支持：支持为不同字段配置不同的密钥
缓存支持：可选缓存加解密结果提升性能
线程安全：所有组件都设计为线程安全
完整异常处理：提供详细的异常信息和日志
国密算法支持：内置SM4国密算法支持
配置灵活：支持通配符匹配字段、缓存配置等

## 注意事项：
密钥管理：生产环境中应使用安全的密钥管理系统
数据库索引：加密字段无法直接使用索引，模糊查询需要特殊处理
数据类型：目前仅支持String类型字段加密
性能影响：加解密操作会有一定的性能开销

这个完整的工具类可以直接集成到Spring Boot项目中，通过简单的配置即可实现对数据库敏感字段的自动加解密保护。