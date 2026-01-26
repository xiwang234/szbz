package xw.szbz.cn.model;

/**
 * Web应用登录请求
 */
public class WebLoginRequest {
    
    private String email;
    private String password;
    private String deviceId;
    
    public WebLoginRequest() {
    }
    
    public WebLoginRequest(String email, String password, String deviceId) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
