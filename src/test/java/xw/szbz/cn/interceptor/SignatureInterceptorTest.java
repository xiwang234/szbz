package xw.szbz.cn.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import xw.szbz.cn.util.PasswordHashUtil;
import xw.szbz.cn.util.SignatureUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名验证拦截器测试
 */
class SignatureInterceptorTest {

    private SignatureInterceptor signatureInterceptor;
    private SignatureUtil signatureUtil;
    private ObjectMapper objectMapper;
    private PasswordHashUtil passwordHashUtil;

    private static final String SIGN_SECRET = "szbz-api-sign-key-2026";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordHashUtil = new PasswordHashUtil();
        objectMapper = new ObjectMapper();
        signatureUtil = new SignatureUtil(passwordHashUtil, objectMapper);
        signatureInterceptor = new SignatureInterceptor(signatureUtil, objectMapper);
    }

    @Test
    void testExcludedPaths_RegisterShouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/register");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertTrue(result, "注册接口应该被排除，不需要签名验证");
    }

    @Test
    void testExcludedPaths_BaZiShouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bazi/generate");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertTrue(result, "八字接口应该被排除，不需要签名验证");
    }

    @Test
    void testMissingTimestamp_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");
        request.addHeader("X-Sign-Nonce", "a".repeat(32));
        request.addHeader("X-Sign", "test-sign");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "缺少 timestamp 应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testMissingNonce_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");
        request.addHeader("X-Sign-Timestamp", String.valueOf(System.currentTimeMillis()));
        request.addHeader("X-Sign", "test-sign");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "缺少 nonce 应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testMissingSign_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");
        request.addHeader("X-Sign-Timestamp", String.valueOf(System.currentTimeMillis()));
        request.addHeader("X-Sign-Nonce", "a".repeat(32));

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "缺少 sign 应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testInvalidTimestampFormat_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");
        request.addHeader("X-Sign-Timestamp", "invalid-timestamp");
        request.addHeader("X-Sign-Nonce", "a".repeat(32));
        request.addHeader("X-Sign", "test-sign");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "非法的 timestamp 格式应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testInvalidNonceLength_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");
        request.addHeader("X-Sign-Timestamp", String.valueOf(System.currentTimeMillis()));
        request.addHeader("X-Sign-Nonce", "short-nonce"); // 不是32位
        request.addHeader("X-Sign", "test-sign");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "nonce 长度不是32位应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testExpiredTimestamp_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/web-auth/login");

        // 使用6分钟前的时间戳（超过5分钟有效期）
        long expiredTimestamp = System.currentTimeMillis() - (6 * 60 * 1000);
        request.addHeader("X-Sign-Timestamp", String.valueOf(expiredTimestamp));
        request.addHeader("X-Sign-Nonce", "a".repeat(32));
        request.addHeader("X-Sign", "test-sign");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "过期的 timestamp 应该验证失败");
        assertEquals(403, response.getStatus());
    }

    @Test
    void testValidSignature_ShouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/web-auth/me");

        // 生成有效的签名
        long timestamp = System.currentTimeMillis();
        String nonce = "a".repeat(32);
        Map<String, String> params = new HashMap<>();
        String sign = signatureUtil.generateSignature(params, String.valueOf(timestamp), nonce, SIGN_SECRET);

        request.addHeader("X-Sign-Timestamp", String.valueOf(timestamp));
        request.addHeader("X-Sign-Nonce", nonce);
        request.addHeader("X-Sign", sign);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertTrue(result, "有效的签名应该验证通过");
    }

    @Test
    void testInvalidSignature_ShouldFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/web-auth/me");

        long timestamp = System.currentTimeMillis();
        String nonce = "a".repeat(32);
        String wrongSign = "wrong-signature-" + "0".repeat(50);

        request.addHeader("X-Sign-Timestamp", String.valueOf(timestamp));
        request.addHeader("X-Sign-Nonce", nonce);
        request.addHeader("X-Sign", wrongSign);

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = signatureInterceptor.preHandle(request, response, null);

        assertFalse(result, "错误的签名应该验证失败");
        assertEquals(403, response.getStatus());
    }
}
