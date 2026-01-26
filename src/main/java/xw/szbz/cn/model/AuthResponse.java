package xw.szbz.cn.model;

/**
 * 认证响应（登录/刷新Token）
 */
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
    private String encryptedUserId;
    private String username;
    private String maskedEmail;
    
    public AuthResponse() {
    }
    
    public AuthResponse(String accessToken, String refreshToken, 
                       Long accessTokenExpiresAt, Long refreshTokenExpiresAt,
                       String encryptedUserId, String username, String maskedEmail) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.encryptedUserId = encryptedUserId;
        this.username = username;
        this.maskedEmail = maskedEmail;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public Long getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }
    
    public void setAccessTokenExpiresAt(Long accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
    
    public Long getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }
    
    public void setRefreshTokenExpiresAt(Long refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
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
}
