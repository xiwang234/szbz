package xw.szbz.cn.controller;

import org.junit.jupiter.api.Test;
import xw.szbz.cn.model.LoginRequest;

/**
 * 登录接口测试
 * 演示如何调用 /api/bazi/login 接口
 */
public class LoginControllerTest {

    @Test
    public void testLoginApiExample() {
        System.out.println("\n========================================");
        System.out.println("  登录接口调用示例");
        System.out.println("========================================\n");
        
        System.out.println("步骤1：从微信小程序获取code");
        System.out.println("  在小程序中调用: wx.login({ success: res => console.log(res.code) })\n");
        
        System.out.println("步骤2：调用登录接口获取Token");
        System.out.println("  URL: POST http://localhost:8080/api/bazi/login");
        System.out.println("  Headers:");
        System.out.println("    Content-Type: application/json");
        System.out.println("  Body:");
        System.out.println("    {");
        System.out.println("      \"code\": \"081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P\"");
        System.out.println("    }\n");
        
        System.out.println("步骤3：接收登录响应");
        System.out.println("  Response:");
        System.out.println("    {");
        System.out.println("      \"code\": 200,");
        System.out.println("      \"message\": \"success\",");
        System.out.println("      \"data\": {");
        System.out.println("        \"token\": \"eyJhbGciOiJIUzM4NCJ9...\",");
        System.out.println("        \"expiresAt\": 1734758400000");
        System.out.println("      }");
        System.out.println("    }\n");
        
        System.out.println("步骤4：使用Token调用分析接口");
        System.out.println("  URL: POST http://localhost:8080/api/bazi/analyze");
        System.out.println("  Headers:");
        System.out.println("    Content-Type: application/json");
        System.out.println("    Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...");
        System.out.println("    X-Timestamp: 1734672000000");
        System.out.println("    X-Sign: abc123...");
        System.out.println("  Body:");
        System.out.println("    {");
        System.out.println("      \"gender\": \"男\",");
        System.out.println("      \"year\": 1984,");
        System.out.println("      \"month\": 11,");
        System.out.println("      \"day\": 27,");
        System.out.println("      \"hour\": 0");
        System.out.println("    }\n");
        
        System.out.println("========================================");
        System.out.println("  CURL 命令示例");
        System.out.println("========================================\n");
        
        System.out.println("# 1. 登录获取Token");
        System.out.println("curl -X POST http://localhost:8080/api/bazi/login \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println("  -d '{\"code\": \"081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P\"}'\n");
        
        System.out.println("# 2. 使用Token调用分析接口");
        System.out.println("curl -X POST http://localhost:8080/api/bazi/analyze \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println("  -H \"Authorization: Bearer YOUR_TOKEN_HERE\" \\");
        System.out.println("  -H \"X-Timestamp: $(date +%s)000\" \\");
        System.out.println("  -H \"X-Sign: YOUR_SIGNATURE\" \\");
        System.out.println("  -d '{");
        System.out.println("    \"gender\": \"男\",");
        System.out.println("    \"year\": 1984,");
        System.out.println("    \"month\": 11,");
        System.out.println("    \"day\": 27,");
        System.out.println("    \"hour\": 0");
        System.out.println("  }'\n");
        
        System.out.println("========================================\n");
    }
    
    @Test
    public void testLoginRequestModel() {
        // 测试LoginRequest模型
        LoginRequest request = new LoginRequest();
        request.setCode("test_code_123");
        
        assert request.getCode().equals("test_code_123");
        
        // 测试构造函数
        LoginRequest request2 = new LoginRequest("test_code_456");
        assert request2.getCode().equals("test_code_456");
        
        System.out.println("✅ LoginRequest模型测试通过");
    }
}
