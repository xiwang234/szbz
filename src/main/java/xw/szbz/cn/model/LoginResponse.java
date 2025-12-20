package xw.szbz.cn.model;

/**
 * 微信小程序登录响应数据
 */
public class LoginResponse {
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * Token过期时间（毫秒时间戳）
     */
    private Long expiresAt;
    
    /**
     * 用户OpenID（可选，根据业务需要决定是否返回）
     */
    private String openId;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public LoginResponse(String token, Long expiresAt, String openId) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.openId = openId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
