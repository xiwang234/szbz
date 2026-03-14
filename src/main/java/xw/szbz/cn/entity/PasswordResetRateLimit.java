package xw.szbz.cn.entity;

import jakarta.persistence.*;

/**
 * 密码重置频率限制表
 * 记录每个邮箱每天调用 request-reset 接口的剩余次数
 */
@Entity
@Table(name = "password_reset_rate_limit")
public class PasswordResetRateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户邮箱（加密存储，与 web_user 保持一致）
     */
    @Column(nullable = false, length = 500)
    private String email;

    /**
     * 当天剩余可用次数
     */
    @Column(name = "remaining_count", nullable = false)
    private Integer remainingCount;

    /**
     * 日期（格式：yyyyMMdd，如 20260314）
     */
    @Column(name = "date_key", nullable = false, length = 8)
    private String dateKey;

    public PasswordResetRateLimit() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRemainingCount() { return remainingCount; }
    public void setRemainingCount(Integer remainingCount) { this.remainingCount = remainingCount; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }
}
