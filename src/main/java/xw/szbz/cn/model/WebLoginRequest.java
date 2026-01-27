package xw.szbz.cn.model;

/**
 * Web应用登录请求
 */
public class WebLoginRequest {

    private String email;
    private String password;  // 前端传入：SHA256(原密码 + 固定盐 + 随机盐)
    private String randomSalt;  // 随机盐
    private String deviceId;

    public WebLoginRequest() {
    }

    public WebLoginRequest(String email, String password, String randomSalt, String deviceId) {
        this.email = email;
        this.password = password;
        this.randomSalt = randomSalt;
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

    public String getRandomSalt() {
        return randomSalt;
    }

    public void setRandomSalt(String randomSalt) {
        this.randomSalt = randomSalt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
