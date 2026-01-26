package xw.szbz.cn.model;

/**
 * 密码重置请求
 */
public class PasswordResetRequest {
    
    private String email;
    
    public PasswordResetRequest() {
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
