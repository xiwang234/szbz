package xw.szbz.cn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.LiuRenRequest;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.SignatureUtil;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("POST /api/bazi/generate - 核心测试 1984年11月23日23点")
    void testGenerateBaZi_19841123_2325() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 23, 23);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"))
                .andExpect(jsonPath("$.yearPillar.fullName").value("甲子"))
                .andExpect(jsonPath("$.monthPillar.fullName").value("乙亥"))
                .andExpect(jsonPath("$.dayPillar.fullName").value("壬戌"))
                .andExpect(jsonPath("$.hourPillar.fullName").value("庚子"))
                .andExpect(jsonPath("$.fullBaZi").value("甲子 乙亥 壬戌 庚子"))
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true))
                .andExpect(jsonPath("$.birthInfo.shiChen").value("子时"))
                .andExpect(jsonPath("$.birthInfo.dayPillarDate").value("1984-11-24"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 成功生成八字")
    void testGenerateBaZi_Success() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"))
                .andExpect(jsonPath("$.yearPillar").exists())
                .andExpect(jsonPath("$.monthPillar").exists())
                .andExpect(jsonPath("$.dayPillar").exists())
                .andExpect(jsonPath("$.hourPillar").exists())
                .andExpect(jsonPath("$.fullBaZi").exists());
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 成功生成八字")
    void testGenerateBaZiGet_Success() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "女")
                        .param("year", "2024")
                        .param("month", "8")
                        .param("day", "20")
                        .param("hour", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("女"))
                .andExpect(jsonPath("$.yearPillar.fullName").exists())
                .andExpect(jsonPath("$.monthPillar.fullName").exists())
                .andExpect(jsonPath("$.dayPillar.fullName").exists())
                .andExpect(jsonPath("$.hourPillar.fullName").exists());
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 1984年11月23日23点")
    void testGenerateBaZiGet_19841123_2325() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "男")
                        .param("year", "1984")
                        .param("month", "11")
                        .param("day", "23")
                        .param("hour", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullBaZi").value("甲子 乙亥 壬戌 庚子"))
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效性别")
    void testGenerateBaZi_InvalidGender() throws Exception {
        BaZiRequest request = new BaZiRequest("", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("性别不能为空"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效年份")
    void testGenerateBaZi_InvalidYear() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1800, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("年份必须在1900-2100之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效月份")
    void testGenerateBaZi_InvalidMonth() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 13, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("月份必须在1-12之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效日期")
    void testGenerateBaZi_InvalidDay() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 32, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("日期必须在1-31之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 无效小时")
    void testGenerateBaZi_InvalidHour() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 2024, 6, 15, 25);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("小时必须在0-23之间"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 英文性别male")
    void testGenerateBaZi_GenderMaleEnglish() throws Exception {
        BaZiRequest request = new BaZiRequest("male", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("男"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 英文性别female")
    void testGenerateBaZi_GenderFemaleEnglish() throws Exception {
        BaZiRequest request = new BaZiRequest("female", 2024, 6, 15, 10);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gender").value("女"));
    }

    @Test
    @DisplayName("GET /api/bazi/generate - 年末跨日测试")
    void testGenerateBaZiGet_EndOfYear() throws Exception {
        mockMvc.perform(get("/api/bazi/generate")
                        .param("gender", "男")
                        .param("year", "1984")
                        .param("month", "12")
                        .param("day", "31")
                        .param("hour", "23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthInfo.adjusted").value(true))
                .andExpect(jsonPath("$.yearPillar.fullName").value("甲子"))
                .andExpect(jsonPath("$.birthInfo.dayPillarDate").value("1985-1-1"));
    }

    @Test
    @DisplayName("POST /api/bazi/generate - 0点不跨日")
    void testGenerateBaZi_0Hour_NoAdjust() throws Exception {
        BaZiRequest request = new BaZiRequest("男", 1984, 11, 24, 0);

        mockMvc.perform(post("/api/bazi/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthInfo.adjusted").value(false))
                .andExpect(jsonPath("$.birthInfo.shiChen").value("子时"))
                .andExpect(jsonPath("$.dayPillar.fullName").value("壬戌"));
    }

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
