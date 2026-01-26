package xw.szbz.cn.model;

/**
 * 用户信息响应
 */
public class UserInfoResponse {
    
    private String encryptedUserId;
    private String username;
    private String maskedEmail;
    private Boolean emailVerified;
    private Boolean active;
    private Long createTime;
    private Long lastLoginTime;
    
    public UserInfoResponse() {
    }
    
    public String getEncryptedUserId() {
        return encryptedUserId;
    }
    
    public void setEncryptedUserId(String encryptedUserId) {
        this.encryptedUserId = encryptedUserId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getMaskedEmail() {
        return maskedEmail;
    }
    
    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
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
}
