package xw.szbz.cn.model;

/**
 * 微信小程序登录请求参数
 */
public class LoginRequest {
    /**
     * 微信小程序登录凭证 code
     * 通过小程序端 wx.login() 获取
     */
    private String code;

    public LoginRequest() {
    }

    public LoginRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
