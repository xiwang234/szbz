package xw.szbz.cn.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import xw.szbz.cn.config.WeChatConfig;
import xw.szbz.cn.model.WeChatSessionResponse;

/**
 * 微信小程序服务测试类 - 真实API调用版本
 * 测试 code2Session 方法调用真实的微信API
 * 
 * 注意：
 * 1. 需要真实有效的微信小程序code（从小程序前端wx.login获取）
 * 2. code只能使用一次，5分钟内有效
 * 3. 需要在application.properties中配置真实的appId和appSecret
 */
public class WeChatServiceTest {

    private WeChatConfig weChatConfig;
    private ObjectMapper objectMapper;
    private WeChatService weChatService;

    @BeforeEach
    void setUp() {
        // 创建真实的配置对象
        weChatConfig = new WeChatConfig();
        // 从application.properties读取配置
        weChatConfig.setAppId("wx4725394b76c26ee2");
        weChatConfig.setAppSecret("eb695e6713208cec6925e9248cbf295a");
        
        // 创建真实的 ObjectMapper
        objectMapper = new ObjectMapper();
        
        // 创建真实的 RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        
        // 使用真实对象创建服务
        weChatService = new WeChatService(weChatConfig, objectMapper, restTemplate);
    }

    /**
     * 测试成功获取 openId 和 sessionKey
     * 
     * 使用步骤：
     * 1. 在微信开发者工具或真实小程序中调用 wx.login() 获取code
     * 2. 将获取到的code替换下面的testCode
     * 3. 立即运行此测试（code 5分钟内有效，且只能使用一次）
     */
    @Test
    public void testCode2Session_Success() throws Exception {
        System.out.println("\n=== 测试真实微信API调用 ===");
        System.out.println("提示：请先从微信小程序获取真实的code，然后替换下面的testCode");
        System.out.println("获取code的方法：在小程序中调用 wx.login({ success: res => console.log(res.code) })");
        System.out.println();
        
        // ⚠️ 请在这里替换为从微信小程序前端获取的真实code
        // code示例格式: "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P"
        String testCode = "0b14PYGa1HeQPK03qwIa16pHQS34PYGV";
        
        // 检查是否已替换code
        if (testCode.equals("请替换为真实的微信小程序code")) {
            System.out.println("⚠️  警告：testCode未替换为真实code，跳过测试");
            System.out.println("请按以下步骤操作：");
            System.out.println("1. 打开微信开发者工具");
            System.out.println("2. 在控制台输入: wx.login({ success: res => console.log('code:', res.code) })");
            System.out.println("3. 复制输出的code");
            System.out.println("4. 替换上面的testCode变量");
            System.out.println("5. 立即运行测试（code只能使用一次，5分钟有效）");
            return; // 跳过测试
        }
        
        try {
            // 执行真实的微信API调用
            System.out.println("正在调用微信API...");
            System.out.println("AppID: " + weChatConfig.getAppId());
            System.out.println("Code: " + testCode);
            
            WeChatSessionResponse response = weChatService.code2Session(testCode);
            
            // 验证结果
            assertNotNull(response, "响应不应为null");
            assertTrue(response.isSuccess(), "调用应该成功");
            assertNotNull(response.getOpenId(), "openId不应为null");
            assertNotNull(response.getSessionKey(), "sessionKey不应为null");
            
            // 打印结果
            System.out.println("\n✅ 微信API调用成功！");
            System.out.println("OpenID: " + response.getOpenId());
            System.out.println("SessionKey: " + response.getSessionKey());
            if (response.getUnionId() != null) {
                System.out.println("UnionID: " + response.getUnionId());
            }
            
        } catch (RuntimeException e) {
            System.err.println("\n❌ 微信API调用失败：" + e.getMessage());
            System.err.println("\n可能的原因：");
            System.err.println("1. code已过期（5分钟有效期）");
            System.err.println("2. code已被使用过（每个code只能使用一次）");
            System.err.println("3. AppID或AppSecret配置错误");
            System.err.println("4. 网络连接问题");
            System.err.println("\n请获取新的code后重试");
            throw e; // 重新抛出异常以便测试失败
        }
    }

    /**
     * 测试 getOpenId 简化方法
     * 同样需要真实的code
     */
    @Test
    public void testGetOpenId_Success() throws Exception {
        System.out.println("\n=== 测试 getOpenId 方法 ===");
        
        // ⚠️ 请在这里替换为从微信小程序前端获取的真实code
        String testCode = "请替换为真实的微信小程序code";
        
        if (testCode.equals("请替换为真实的微信小程序code")) {
            System.out.println("⚠️  警告：testCode未替换为真实code，跳过测试");
            return;
        }
        
        try {
            String openId = weChatService.getOpenId(testCode);
            
            assertNotNull(openId, "openId不应为null");
            assertTrue(openId.startsWith("o"), "openId通常以'o'开头");
            
            System.out.println("✅ getOpenId 方法测试成功");
            System.out.println("OpenID: " + openId);
            
        } catch (RuntimeException e) {
            System.err.println("❌ 调用失败：" + e.getMessage());
            throw e;
        }
    }
}
