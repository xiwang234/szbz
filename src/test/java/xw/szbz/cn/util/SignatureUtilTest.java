package xw.szbz.cn.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名工具类测试
 * 用于验证前后端签名算法一致性
 */
@SpringBootTest
public class SignatureUtilTest {

    @Autowired
    private SignatureUtil signatureUtil;

    /**
     * 测试用例1: 基本参数签名
     * 用于与前端对比验证
     */
    @Test
    public void testCase1_BasicParams() {
        System.out.println("========== 测试用例1: 基本参数 ==========");
        
        Map<String, Object> params = new HashMap<>();
        params.put("openId", "test_openid_123456");
        params.put("gender", "male");
        params.put("year", 2014);
        params.put("month", 6);
        params.put("day", 15);
        params.put("hour", 11);
        
        long timestamp = 1702800000000L;
        String sign = signatureUtil.generateSignature(params, timestamp);
        
        System.out.println("参数: " + params);
        System.out.println("时间戳: " + timestamp);
        System.out.println("后端签名: " + sign);
        System.out.println();
        
        assertNotNull(sign);
        assertEquals(32, sign.length()); // MD5签名长度为32位
    }

    /**
     * 测试用例2: 女性参数签名
     */
    @Test
    public void testCase2_FemaleParams() {
        System.out.println("========== 测试用例2: 女性参数 ==========");
        
        Map<String, Object> params = new HashMap<>();
        params.put("openId", "wxuser_987654");
        params.put("gender", "female");
        params.put("year", 1990);
        params.put("month", 12);
        params.put("day", 25);
        params.put("hour", 8);
        
        long timestamp = 1702900000000L;
        String sign = signatureUtil.generateSignature(params, timestamp);
        
        System.out.println("参数: " + params);
        System.out.println("时间戳: " + timestamp);
        System.out.println("后端签名: " + sign);
        System.out.println();
        
        assertNotNull(sign);
    }

    /**
     * 测试用例3: 边界值签名
     */
    @Test
    public void testCase3_BoundaryValues() {
        System.out.println("========== 测试用例3: 边界值 ==========");
        
        Map<String, Object> params = new HashMap<>();
        params.put("openId", "boundary_test");
        params.put("gender", "male");
        params.put("year", 1950);
        params.put("month", 1);
        params.put("day", 1);
        params.put("hour", 0);
        
        long timestamp = System.currentTimeMillis();
        String sign = signatureUtil.generateSignature(params, timestamp);
        
        System.out.println("参数: " + params);
        System.out.println("时间戳: " + timestamp);
        System.out.println("后端签名: " + sign);
        System.out.println();
        
        assertNotNull(sign);
    }

    /**
     * 测试签名验证功能
     */
    @Test
    public void testSignatureVerification() {
        System.out.println("========== 测试签名验证 ==========");
        
        Map<String, Object> params = new HashMap<>();
        params.put("openId", "verify_test");
        params.put("gender", "male");
        params.put("year", 2000);
        params.put("month", 1);
        params.put("day", 1);
        params.put("hour", 12);
        
        long timestamp = System.currentTimeMillis();
        
        // 生成签名
        String sign = signatureUtil.generateSignature(params, timestamp);
        System.out.println("生成签名: " + sign);
        
        // 验证正确签名
        boolean isValid = signatureUtil.verifySignature(params, timestamp, sign);
        System.out.println("验证结果(正确签名): " + isValid);
        assertTrue(isValid, "正确签名应该验证通过");
        
        // 验证错误签名
        boolean isInvalid = signatureUtil.verifySignature(params, timestamp, "wrong_signature");
        System.out.println("验证结果(错误签名): " + isInvalid);
        assertFalse(isInvalid, "错误签名应该验证失败");
        
        System.out.println();
    }

    /**
     * 测试时间戳验证功能
     */
    @Test
    public void testTimestampValidation() {
        System.out.println("========== 测试时间戳验证 ==========");
        
        // 当前时间戳(有效)
        long validTimestamp = System.currentTimeMillis();
        boolean isValid = signatureUtil.validateTimestamp(validTimestamp);
        System.out.println("当前时间戳: " + validTimestamp + " -> " + isValid);
        assertTrue(isValid, "当前时间戳应该有效");
        
        // 5秒前的时间戳(无效)
        long expiredTimestamp = System.currentTimeMillis() - 5000;
        boolean isExpired = signatureUtil.validateTimestamp(expiredTimestamp);
        System.out.println("5秒前时间戳: " + expiredTimestamp + " -> " + isExpired);
        assertFalse(isExpired, "5秒前的时间戳应该无效");
        
        // 1秒前的时间戳(有效)
        long recentTimestamp = System.currentTimeMillis() - 1000;
        boolean isRecent = signatureUtil.validateTimestamp(recentTimestamp);
        System.out.println("1秒前时间戳: " + recentTimestamp + " -> " + isRecent);
        assertTrue(isRecent, "1秒前的时间戳应该有效");
        
        System.out.println();
    }

    /**
     * 运行所有测试并打印对比信息
     */
    @Test
    public void runAllTestsForComparison() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("前后端签名算法对比测试");
        System.out.println("=".repeat(60) + "\n");
        
        testCase1_BasicParams();
        testCase2_FemaleParams();
        testCase3_BoundaryValues();
        
        System.out.println("=".repeat(60));
        System.out.println("请将上述签名与前端生成的签名进行对比!");
        System.out.println("前端测试方法: 在微信开发者工具中运行 test-crypto.js");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * 打印签名生成详细步骤(用于调试)
     */
    @Test
    public void printSignatureGenerationSteps() {
        System.out.println("\n========== 签名生成详细步骤 ==========\n");
        
        Map<String, Object> params = new HashMap<>();
        params.put("openId", "test_openid_123456");
        params.put("gender", "male");
        params.put("year", 2014);
        params.put("month", 6);
        params.put("day", 15);
        params.put("hour", 11);
        
        long timestamp = 1702800000000L;
        
        System.out.println("步骤1: 原始参数");
        System.out.println(params);
        System.out.println();
        
        System.out.println("步骤2: 添加时间戳并排序");
        System.out.println("参数按key字母顺序排序后:");
        System.out.println("day=15");
        System.out.println("gender=male");
        System.out.println("hour=11");
        System.out.println("month=6");
        System.out.println("openId=test_openid_123456");
        System.out.println("timestamp=" + timestamp);
        System.out.println("year=2014");
        System.out.println();
        
        System.out.println("步骤3: 拼接参数字符串");
        String paramStr = "day=15&gender=male&hour=11&month=6&openId=test_openid_123456&timestamp=" + timestamp + "&year=2014&key=szbz-api-sign-key-2024";
        System.out.println(paramStr);
        System.out.println();
        
        System.out.println("步骤4: MD5加密");
        String sign = signatureUtil.generateSignature(params, timestamp);
        System.out.println("最终签名: " + sign);
        System.out.println();
    }
}
