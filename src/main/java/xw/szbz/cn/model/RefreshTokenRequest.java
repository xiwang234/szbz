package xw.szbz.cn.model;

/**
 * 刷新Token请求
 */
public class RefreshTokenRequest {
    
    private String refreshToken;
    private String deviceId;
    
    public RefreshTokenRequest() {
    }
    
    public RefreshTokenRequest(String refreshToken, String deviceId) {
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
