package xw.szbz.cn.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import xw.szbz.cn.model.LiuRenRequest;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.SignatureUtil;

/**
 * BaZiController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class BaZiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SignatureUtil signatureUtil;

    // ========== 六壬预测接口测试 ==========

    @Test
    @DisplayName("POST /api/bazi/wenji - 成功预测（完整参数）")
    void testPredictLiuRen_Success() throws Exception {
        // 准备测试数据
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问近期投资项目能否成功？");
        request.setBackground("计划投资一个新项目，投入资金约50万元，需要评估风险。");
        request.setBirthYear(1984);
        request.setGender("男");

        // 生成JWT Token
        String openId = "test-openid-123456";
        String token = jwtUtil.generateToken(openId);

        // 生成时间戳和签名
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        // 执行请求
        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.prediction").exists())
                .andExpect(jsonPath("$.data.courseInfo").exists())
                .andExpect(jsonPath("$.data.question").value("问近期投资项目能否成功？"))
                .andExpect(jsonPath("$.data.birthInfo").exists());
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少问题参数")
    void testPredictLiuRen_MissingQuestion() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setBirthYear(1990);
        request.setGender("女");

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("占问事项不能为空"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少出生年份")
    void testPredictLiuRen_MissingBirthYear() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问婚姻运势？");
        request.setGender("女");

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("出生年份不能为空"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少性别")
    void testPredictLiuRen_MissingGender() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问事业发展？");
        request.setBirthYear(1995);

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("性别不能为空"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 性别参数非法")
    void testPredictLiuRen_InvalidGender() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问健康状况？");
        request.setBirthYear(1988);
        request.setGender("未知");

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("性别只能是'男'或'女'"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少JWT Token")
    void testPredictLiuRen_MissingToken() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问财运？");
        request.setBirthYear(1992);
        request.setGender("男");

        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未提供认证Token"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 无效的JWT Token")
    void testPredictLiuRen_InvalidToken() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问学业？");
        request.setBirthYear(2000);
        request.setGender("女");

        String invalidToken = "invalid.token.string";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + invalidToken)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token无效或已过期"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少时间戳")
    void testPredictLiuRen_MissingTimestamp() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问感情？");
        request.setBirthYear(1985);
        request.setGender("男");

        String token = jwtUtil.generateToken("test-openid");
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, System.currentTimeMillis());

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少时间戳参数"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 时间戳过期")
    void testPredictLiuRen_ExpiredTimestamp() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问工作？");
        request.setBirthYear(1993);
        request.setGender("女");

        String token = jwtUtil.generateToken("test-openid");
        long expiredTimestamp = System.currentTimeMillis() - 3000; // 3秒前
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, expiredTimestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", expiredTimestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求已过期，时间戳超过2秒"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 缺少签名")
    void testPredictLiuRen_MissingSignature() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问财富？");
        request.setBirthYear(1987);
        request.setGender("男");

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("缺少签名参数"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 签名验证失败")
    void testPredictLiuRen_InvalidSignature() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问运势？");
        request.setBirthYear(1991);
        request.setGender("女");

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        String invalidSign = "invalid-signature-string";

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", invalidSign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("签名验证失败，参数可能被篡改"));
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 无背景信息（可选参数为空）")
    void testPredictLiuRen_NoBackground() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问今年运势如何？");
        request.setBirthYear(1986);
        request.setGender("男");
        // background不设置，默认为null

        String token = jwtUtil.generateToken("test-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.prediction").exists())
                .andExpect(jsonPath("$.data.courseInfo").exists());
    }

    @Test
    @DisplayName("POST /api/bazi/wenji - 女性命主测试")
    void testPredictLiuRen_FemaleGender() throws Exception {
        LiuRenRequest request = new LiuRenRequest();
        request.setQuestion("问婚姻何时到来？");
        request.setBackground("目前单身，希望了解姻缘何时到来。");
        request.setBirthYear(1995);
        request.setGender("女");

        String token = jwtUtil.generateToken("test-female-openid");
        long timestamp = System.currentTimeMillis();
        Map<String, Object> params = signatureUtil.objectToMap(request);
        String sign = signatureUtil.generateSignature(params, timestamp);

        mockMvc.perform(post("/api/bazi/wenji")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .header("X-Timestamp", timestamp)
                        .header("X-Sign", sign)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.birthInfo").value("1995年乙亥年女命"));
    }
}
