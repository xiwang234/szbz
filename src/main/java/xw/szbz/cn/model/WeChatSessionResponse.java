package xw.szbz.cn.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信小程序 code2Session 响应数据
 * 文档: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
 */
public class WeChatSessionResponse {
    
    @JsonProperty("openid")
    private String openId;
    
    @JsonProperty("session_key")
    private String sessionKey;
    
    @JsonProperty("unionid")
    private String unionId;
    
    @JsonProperty("errcode")
    private Integer errCode;
    
    @JsonProperty("errmsg")
    private String errMsg;

    public WeChatSessionResponse() {
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return errCode == null && openId != null && !openId.isEmpty();
    }
}
