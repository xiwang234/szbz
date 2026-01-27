package xw.szbz.cn.model;

/**
 * 用户注册请求
 */
public class RegisterRequest {

    private String username;
    private String email;
    private String password;  // 前端传入：SHA256(原密码 + 固定盐)

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
