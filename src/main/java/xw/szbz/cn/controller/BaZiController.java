package xw.szbz.cn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.model.BaZiAnalysisResponse;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.GeminiService;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.SignatureUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 四柱八字API控制器
 */
@RestController
@RequestMapping("/api/bazi")
public class BaZiController {

    private final BaZiService baZiService;
    private final GeminiService geminiService;
    private final JwtUtil jwtUtil;
    private final SignatureUtil signatureUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public BaZiController(BaZiService baZiService, 
                          GeminiService geminiService,
                          JwtUtil jwtUtil,
                          SignatureUtil signatureUtil,
                          RedisTemplate<String, Object> redisTemplate,
                          ObjectMapper objectMapper) {
        this.baZiService = baZiService;
        this.geminiService = geminiService;
        this.jwtUtil = jwtUtil;
        this.signatureUtil = signatureUtil;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成四柱八字
     *
     * @param request 请求参数（性别、出生年月日时）
     * @return 四柱八字结果
     */
    @PostMapping("/generate")
    public ResponseEntity<BaZiResult> generateBaZi(@RequestBody BaZiRequest request) {
        validateRequest(request);
        BaZiResult result = baZiService.calculate(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET方式生成四柱八字
     *
     * @param gender 性别
     * @param year   出生年
     * @param month  出生月
     * @param day    出生日
     * @param hour   出生时
     * @return 四柱八字结果
     */
    @GetMapping("/generate")
    public ResponseEntity<BaZiResult> generateBaZiGet(
            @RequestParam String gender,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam int hour) {
        BaZiRequest request = new BaZiRequest(gender, year, month, day, hour);
        validateRequest(request);
        BaZiResult result = baZiService.calculate(request);
        return ResponseEntity.ok(result);
    }

    private void validateRequest(BaZiRequest request) {
        if (request.getGender() == null || request.getGender().isEmpty()) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (request.getYear() < 1900 || request.getYear() > 2100) {
            throw new IllegalArgumentException("年份必须在1900-2100之间");
        }
        if (request.getMonth() < 1 || request.getMonth() > 12) {
            throw new IllegalArgumentException("月份必须在1-12之间");
        }
        if (request.getDay() < 1 || request.getDay() > 31) {
            throw new IllegalArgumentException("日期必须在1-31之间");
        }
        if (request.getHour() < 0 || request.getHour() > 23) {
            throw new IllegalArgumentException("小时必须在0-23之间");
        }
    }

    /**
     * 生成八字并使用 Gemini AI 进行分析（增强版：含JWT、Redis缓存、签名验证）
     *
     * @param request   请求参数（openId、性别、出生年月日时）
     * @param timestamp 时间戳（Header）
     * @param sign      签名（Header）
     * @return 包含八字结果、AI分析和JWT Token的响应
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<BaZiAnalysisResponse>> analyzeBaZiWithAI(
            @RequestBody BaZiRequest request,
            @RequestHeader(value = "X-Timestamp", required = false) Long timestamp,
            @RequestHeader(value = "X-Sign", required = false) String sign) {

        try {
            // ===== 任务1: 验证OpenId =====
            if (request.getOpenId() == null || request.getOpenId().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "openId不能为空"));
            }

            // ===== 任务4: 验证时间戳（超过2秒返回错误） =====
            if (timestamp == null) {
                return ResponseEntity.ok(ApiResponse.error(400, "缺少时间戳参数"));
            }
            if (!signatureUtil.validateTimestamp(timestamp)) {
                return ResponseEntity.ok(ApiResponse.error(400, "请求已过期，时间戳超过2秒"));
            }

            // ===== 任务4: 验证签名（防止参数被篡改） =====
            if (sign == null || sign.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "缺少签名参数"));
            }
            Map<String, Object> params = signatureUtil.objectToMap(request);
            if (!signatureUtil.verifySignature(params, timestamp, sign)) {
                return ResponseEntity.ok(ApiResponse.error(400, "签名验证失败，参数可能被篡改"));
            }

            // 验证基本参数
            validateRequest(request);

            // ===== 任务3: 检查Redis缓存 =====
            String cacheKey = generateCacheKey(request);
            Object cachedResult = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedResult != null) {
                // 缓存命中，直接返回
                System.out.println("缓存命中: " + cacheKey);
                BaZiAnalysisResponse cachedResponse = objectMapper.convertValue(cachedResult, BaZiAnalysisResponse.class);
                
                // ===== 任务2: 生成JWT Token =====
                String token = jwtUtil.generateToken(request.getOpenId());
                
                return ResponseEntity.ok(ApiResponse.success(cachedResponse, token));
            }

            // 1. 计算八字
            BaZiResult baZiResult = baZiService.calculate(request);

            // 2. 使用 Gemini AI 分析（返回JSON格式）
            Object aiAnalysis = geminiService.analyzeBaZi(baZiResult);

            // 3. 构建响应数据
            BaZiAnalysisResponse responseData = new BaZiAnalysisResponse(baZiResult, aiAnalysis);

            // ===== 任务3: 将结果存入Redis缓存，过期时间3天 =====
            redisTemplate.opsForValue().set(cacheKey, responseData, 3, TimeUnit.DAYS);
            System.out.println("结果已缓存: " + cacheKey);

            // ===== 任务2: 生成JWT Token =====
            String token = jwtUtil.generateToken(request.getOpenId());

            // ===== 任务5: 返回JSON格式，包含Token =====
            return ResponseEntity.ok(ApiResponse.success(responseData, token));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * GET方式生成八字并使用 Gemini AI 进行分析
     *
     * @param gender 性别
     * @param year   出生年
     * @param month  出生月
     * @param day    出生日
     * @param hour   出生时
     * @return 包含八字结果和 AI 分析的响应
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeBaZiWithAIGet(
            @RequestParam String gender,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam int hour) {
        BaZiRequest request = new BaZiRequest(gender, year, month, day, hour);
        validateRequest(request);

        // 1. 计算八字
        BaZiResult baZiResult = baZiService.calculate(request);

        // 2. 使用 Gemini AI 分析
        Object aiAnalysis = geminiService.analyzeBaZi(baZiResult);

        // 3. 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("baziResult", baZiResult);
        response.put("aiAnalysis", aiAnalysis);

        return ResponseEntity.ok(response);
    }

    /**
     * 生成缓存Key
     * 格式: bazi:openId:gender:year:month:day:hour
     */
    private String generateCacheKey(BaZiRequest request) {
        return String.format("bazi:%s:%s:%d:%d:%d:%d",
                request.getOpenId(),
                request.getGender(),
                request.getYear(),
                request.getMonth(),
                request.getDay(),
                request.getHour());
    }
}
