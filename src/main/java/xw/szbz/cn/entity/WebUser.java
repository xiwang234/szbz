package xw.szbz.cn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Web应用用户实体
 * 支持邮箱密码登录，独立于微信小程序用户系统
 */
@Entity
@Table(name = "web_user")
public class WebUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名（唯一，用于登录和展示）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 邮箱（加密存储，唯一，用于登录）
     * 使用AES-256-GCM加密存储Base64格式
     */
    @Column(nullable = false, unique = true, length = 500)
    private String email;
    
    /**
     * 密码哈希（BCrypt加密存储）
     */
    @Column(nullable = false, length = 100)
    private String passwordHash;
    
    /**
     * 邮箱验证状态
     */
    @Column(nullable = false)
    private Boolean emailVerified = false;
    
    /**
     * 邮箱验证令牌
     */
    @Column(length = 100)
    private String emailVerificationToken;
    
    /**
     * 邮箱验证令牌过期时间（13位毫秒时间戳）
     */
    private Long emailVerificationExpiry;

    /**
     * 密码重置令牌
     */
    @Column(length = 100)
    private String passwordResetToken;

    /**
     * 密码重置令牌过期时间（13位毫秒时间戳）
     */
    private Long passwordResetExpiry;
    
    /**
     * 账户状态（true=正常，false=禁用）
     */
    @Column(nullable = false)
    private Boolean active = true;
    
    /**
     * 创建时间（13位时间戳）
     */
    @Column(nullable = false)
    private Long createTime;
    
    /**
     * 最后登录时间（13位时间戳）
     */
    private Long lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @Column(length = 50)
    private String lastLoginIp;
    
    // 构造函数
    public WebUser() {
        this.createTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }
    
    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }
    
    public Long getEmailVerificationExpiry() {
        return emailVerificationExpiry;
    }

    public void setEmailVerificationExpiry(Long emailVerificationExpiry) {
        this.emailVerificationExpiry = emailVerificationExpiry;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Long getPasswordResetExpiry() {
        return passwordResetExpiry;
    }

    public void setPasswordResetExpiry(Long passwordResetExpiry) {
        this.passwordResetExpiry = passwordResetExpiry;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Long getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public String getLastLoginIp() {
        return lastLoginIp;
    }
    
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
    
    /**
     * 获取LocalDateTime格式的创建时间（用于加密ID生成）
     */
    public LocalDateTime getCreateTimeAsLocalDateTime() {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(this.createTime),
            java.time.ZoneOffset.UTC
        );
    }
}
