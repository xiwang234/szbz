package xw.szbz.cn.entity;

import jakarta.persistence.*;

/**
 * 用户随机盐信息实体
 * 存储登录时使用的随机盐，与用户邮箱绑定
 */
@Entity
@Table(name = "user_salt_info")
public class UserSaltInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户邮箱（关联账号）
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * 随机盐
     */
    @Column(nullable = false, length = 64)
    private String salt;

    /**
     * 状态：0-未使用，1-已使用
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建时间（13位毫秒时间戳）
     */
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    /**
     * 有效期时间戳（创建时间 + 5分钟）
     */
    @Column(name = "use_time", nullable = false)
    private Long useTime;

    // 构造函数
    public UserSaltInfo() {
    }

    public UserSaltInfo(String email, String salt, Integer status, Long createTime, Long useTime) {
        this.email = email;
        this.salt = salt;
        this.status = status;
        this.createTime = createTime;
        this.useTime = useTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUseTime() {
        return useTime;
    }

    public void setUseTime(Long useTime) {
        this.useTime = useTime;
    }
}
