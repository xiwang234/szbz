package xw.szbz.cn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xw.szbz.cn.config.WeChatConfig;
import xw.szbz.cn.model.WeChatSessionResponse;

/**
 * 微信小程序服务类
 * 提供小程序登录、用户信息获取等功能
 */
@Service
public class WeChatService {

    private static final String CODE_2_SESSION_URL = 
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public WeChatService(WeChatConfig weChatConfig, ObjectMapper objectMapper) {
        this(weChatConfig, objectMapper, new RestTemplate());
    }

    /**
     * 构造函数（用于测试时注入 Mock RestTemplate）
     */
    WeChatService(WeChatConfig weChatConfig, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.weChatConfig = weChatConfig;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * 通过 code 换取 openId 和 session_key
     * 文档: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
     *
     * @param code 小程序登录时获取的 code
     * @return 包含 openId、sessionKey 的响应对象
     * @throws RuntimeException 当调用失败或参数错误时
     */
    public WeChatSessionResponse code2Session(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("code 不能为空");
        }

        try {
            // 调用微信接口
            String response = restTemplate.getForObject(
                    CODE_2_SESSION_URL,
                    String.class,
                    weChatConfig.getAppId(),
                    weChatConfig.getAppSecret(),
                    code
            );

            // 解析响应
            WeChatSessionResponse sessionResponse = objectMapper.readValue(response, WeChatSessionResponse.class);

            // 检查是否成功
            if (!sessionResponse.isSuccess()) {
                String errorMsg = String.format("微信登录失败: errcode=%d, errmsg=%s", 
                        sessionResponse.getErrCode(), 
                        sessionResponse.getErrMsg());
                throw new RuntimeException(errorMsg);
            }

            return sessionResponse;

        } catch (Exception e) {
            throw new RuntimeException("调用微信 code2Session 接口失败: " + e.getMessage(), e);
        }
    }

    /**
     * 仅获取 openId（简化方法）
     *
     * @param code 小程序登录时获取的 code
     * @return openId
     */
    public String getOpenId(String code) {
        WeChatSessionResponse response = code2Session(code);
        return response.getOpenId();
    }
}
