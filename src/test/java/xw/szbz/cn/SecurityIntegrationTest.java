package xw.szbz.cn;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.SignatureUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全功能集成测试
 * 测试JWT、签名验证、缓存等功能
 */
@SpringBootTest
public class SecurityIntegrationTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SignatureUtil signatureUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testJwtGeneration() {
        String openId = "oABCD1234567890";
        
        // 生成Token
        String token = jwtUtil.generateToken(openId);
        System.out.println("Generated JWT Token: " + token);
        
        // 验证Token
        boolean isValid = jwtUtil.validateToken(token);
        System.out.println("Token is valid: " + isValid);
        
        // 从Token中提取OpenId
        String extractedOpenId = jwtUtil.getOpenIdFromToken(token);
        System.out.println("Extracted OpenId: " + extractedOpenId);
        
        assert isValid;
        assert openId.equals(extractedOpenId);
    }

    @Test
    public void testSignatureGeneration() {
        // 准备参数（使用微信小程序登录 code）
        Map<String, Object> params = new HashMap<>();
        params.put("code", "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");  // 微信小程序登录凭证
        params.put("gender", "男");
        params.put("year", 1984);
        params.put("month", 11);
        params.put("day", 27);
        params.put("hour", 0);
        
        long timestamp = System.currentTimeMillis();
        
        // 生成签名
        String signature = signatureUtil.generateSignature(params, timestamp);
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Generated Signature: " + signature);
        
        // 验证签名
        boolean isValid = signatureUtil.verifySignature(params, timestamp, signature);
        System.out.println("Signature is valid: " + isValid);
        
        assert isValid;
    }

    @Test
    public void testTimestampValidation() {
        long currentTimestamp = System.currentTimeMillis();
        
        // 测试有效时间戳（当前时间）
        boolean isValid1 = signatureUtil.validateTimestamp(currentTimestamp);
        System.out.println("Current timestamp is valid: " + isValid1);
        assert isValid1;
        
        // 测试无效时间戳（3秒前）
        long oldTimestamp = currentTimestamp - 3000;
        boolean isValid2 = signatureUtil.validateTimestamp(oldTimestamp);
        System.out.println("Old timestamp (3s ago) is valid: " + isValid2);
        assert !isValid2;
        
        // 测试边界情况（1.5秒前）
        long borderTimestamp = currentTimestamp - 1500;
        boolean isValid3 = signatureUtil.validateTimestamp(borderTimestamp);
        System.out.println("Border timestamp (1.5s ago) is valid: " + isValid3);
        assert isValid3;
    }

    @Test
    public void testCompleteRequestFlow() {
        // 准备完整的请求参数（微信小程序登录版）
        BaZiRequest request = new BaZiRequest();
        request.setCode("081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");  // 微信登录凭证
        request.setGender("男");
        request.setYear(1984);
        request.setMonth(11);
        request.setDay(27);
        request.setHour(0);
        
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        
        // 转换为Map并生成签名
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String signature = signatureUtil.generateSignature(params, timestamp);
        
        System.out.println("=== 完整请求示例（微信小程序登录） ===");
        System.out.println("Request Body: " + request);
        System.out.println("X-Timestamp: " + timestamp);
        System.out.println("X-Sign: " + signature);
        
        // 验证流程
        boolean timestampValid = signatureUtil.validateTimestamp(timestamp);
        boolean signatureValid = signatureUtil.verifySignature(params, timestamp, signature);
        
        System.out.println("Timestamp Valid: " + timestampValid);
        System.out.println("Signature Valid: " + signatureValid);
        
        if (timestampValid && signatureValid) {
            // 模拟：实际应用中会调用微信接口换取 openId
            String mockOpenId = "oABCD1234567890";  // 模拟从微信获取的 openId
            String token = jwtUtil.generateToken(mockOpenId);
            System.out.println("Generated JWT Token: " + token);
        }
        
        assert timestampValid;
        assert signatureValid;
    }

    @Test
    public void testCurlCommandGeneration() {
        // 准备参数（微信小程序登录版）
        Map<String, Object> params = new HashMap<>();
        params.put("code", "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");  // 微信登录凭证
        params.put("gender", "男");
        params.put("year", 1984);
        params.put("month", 11);
        params.put("day", 27);
        params.put("hour", 0);
        
        long timestamp = System.currentTimeMillis();
        String signature = signatureUtil.generateSignature(params, timestamp);
        
        // 生成curl命令（用于手动测试）
        System.out.println("\n=== CURL测试命令（微信小程序版） ===");
        System.out.println("curl -X POST http://localhost:8080/api/bazi/analyze \\");
        System.out.println("  -H \"Content-Type: application/json\" \\");
        System.out.println("  -H \"X-Timestamp: " + timestamp + "\" \\");
        System.out.println("  -H \"X-Sign: " + signature + "\" \\");
        System.out.println("  -d '{");
        System.out.println("    \"code\": \"081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P\",");
        System.out.println("    \"gender\": \"男\",");
        System.out.println("    \"year\": 1984,");
        System.out.println("    \"month\": 11,");
        System.out.println("    \"day\": 27,");
        System.out.println("    \"hour\": 0");
        System.out.println("  }'");
        System.out.println("\n注意：code 需要从小程序端 wx.login() 获取，且有效期仅5分钟");
    }

    @Test
    public void testPostmanRequestExample() {
        // 准备参数（微信小程序登录版）
        Map<String, Object> params = new HashMap<>();
        params.put("code", "081nBp0w3MqiWf27BQ2w3UWgRg1nBp0P");  // 微信登录凭证
        params.put("gender", "男");
        params.put("year", 1984);
        params.put("month", 11);
        params.put("day", 27);
        params.put("hour", 0);
        
        long timestamp = System.currentTimeMillis();
        String signature = signatureUtil.generateSignature(params, timestamp);
        
        System.out.println("\n=== Postman配置示例（微信小程序版） ===");
        System.out.println("URL: http://localhost:8080/api/bazi/analyze");
        System.out.println("Method: POST");
        System.out.println("\nHeaders:");
        System.out.println("  Content-Type: application/json");
        System.out.println("  X-Timestamp: " + timestamp);
        System.out.println("  X-Sign: " + signature);
        System.out.println("\nBody (raw JSON):");
        try {
            String jsonBody = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(params);
            System.out.println(jsonBody);
            System.out.println("\n⚠️  注意事项：");
            System.out.println("1. code 需要从小程序端通过 wx.login() 获取");
            System.out.println("2. code 有效期仅5分钟，使用后立即失效");
            System.out.println("3. 时间戳有效期为2秒，超时需重新生成签名");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
