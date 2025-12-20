package xw.szbz.cn.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.model.BaZiAnalysisResponse;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.model.BusinessLog;
import xw.szbz.cn.model.LoginRequest;
import xw.szbz.cn.model.LoginResponse;
import xw.szbz.cn.service.BaZiCacheService;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.BusinessLogService;
import xw.szbz.cn.service.GeminiService;
import xw.szbz.cn.service.WeChatService;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.SignatureUtil;

/**
 * 四柱八字API控制器
 */
@RestController
@RequestMapping("/api/bazi")
public class BaZiController {

    private final BaZiService baZiService;
    private final GeminiService geminiService;
    private final WeChatService weChatService;
    private final JwtUtil jwtUtil;
    private final SignatureUtil signatureUtil;
    private final BaZiCacheService cacheService;
    private final BusinessLogService businessLogService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BaZiController(BaZiService baZiService, 
                          GeminiService geminiService,
                          WeChatService weChatService,
                          JwtUtil jwtUtil,
                          SignatureUtil signatureUtil,
                          BaZiCacheService cacheService,
                          BusinessLogService businessLogService,
                          ObjectMapper objectMapper) {
        this.baZiService = baZiService;
        this.geminiService = geminiService;
        this.weChatService = weChatService;
        this.jwtUtil = jwtUtil;
        this.signatureUtil = signatureUtil;
        this.cacheService = cacheService;
        this.businessLogService = businessLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * 微信小程序登录接口
     * 使用微信官方接口将 code 换取 openId，并生成 JWT Token
     *
     * @param request 登录请求（包含微信小程序code）
     * @return 包含JWT Token的登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            // 验证参数
            if (request.getCode() == null || request.getCode().isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(400, "登录凭证code不能为空"));
            }

            // 调用微信官方接口，将 code 换取 openId
            String openId;
            try {
                openId = weChatService.getOpenId(request.getCode());
                System.out.println("微信登录成功，OpenId: " + openId);
            } catch (Exception e) {
                System.err.println("微信登录失败: " + e.getMessage());
                return ResponseEntity.ok(ApiResponse.error(401, "微信登录失败: " + e.getMessage()));
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(openId);
            
            // 计算过期时间
            Long expiresAt = System.currentTimeMillis() + 86400000L; // 24小时后

            // 构建响应（不返回openId，保护用户隐私）
            LoginResponse response = new LoginResponse(token, expiresAt);
            
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            System.err.println("登录服务器错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 生成八字并使用 Gemini AI 进行分析
     * 增强功能：JWT验证 + Caffeine缓存 + 签名验证 + 业务日志
     *
     * @param request   请求参数（性别、出生年月日时）
     * @param token     JWT Token（Header）
     * @param timestamp 时间戳（Header）
     * @param sign      签名（Header）
     * @param httpRequest HTTP请求对象
     * @return 包含AI分析结果的响应
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<BaZiAnalysisResponse>> analyzeBaZiWithAI(
            @RequestBody BaZiRequest request,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Timestamp", required = false) Long timestamp,
            @RequestHeader(value = "X-Sign", required = false) String sign,
            HttpServletRequest httpRequest) {

        long startTime = System.currentTimeMillis();
        BusinessLog businessLog = new BusinessLog();
        String openId = null;
        
        try {
            // 记录请求信息
            businessLog.setRequestIp(getClientIp(httpRequest));
            businessLog.setUserAgent(httpRequest.getHeader("User-Agent"));
            businessLog.setGender(request.getGender());
            businessLog.setYear(request.getYear());
            businessLog.setMonth(request.getMonth());
            businessLog.setDay(request.getDay());
            businessLog.setHour(request.getHour());

            // ===== Step 1: 验证JWT Token =====
            if (token == null || token.isEmpty()) {
                return buildErrorResponse(businessLog, startTime, 401, "未提供认证Token");
            }
            
            // 移除 "Bearer " 前缀（如果存在）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 验证Token有效性
            if (!jwtUtil.validateToken(token)) {
                return buildErrorResponse(businessLog, startTime, 401, "Token无效或已过期");
            }
            
            // 从Token中提取openId
            try {
                openId = jwtUtil.getOpenIdFromToken(token);
                businessLog.setOpenId(openId);
                System.out.println("Token验证成功，OpenId: " + openId);
            } catch (Exception e) {
                return buildErrorResponse(businessLog, startTime, 401, "Token解析失败");
            }

            // ===== Step 2: 验证时间戳（超过2秒返回错误） =====
            if (timestamp == null) {
                return buildErrorResponse(businessLog, startTime, 400, "缺少时间戳参数");
            }
            if (!signatureUtil.validateTimestamp(timestamp)) {
                return buildErrorResponse(businessLog, startTime, 400, "请求已过期，时间戳超过2秒");
            }

            // ===== Step 3: 验证签名（防止参数被篡改） =====
            if (sign == null || sign.isEmpty()) {
                return buildErrorResponse(businessLog, startTime, 400, "缺少签名参数");
            }
            Map<String, Object> params = signatureUtil.objectToMap(request);
            if (!signatureUtil.verifySignature(params, timestamp, sign)) {
                return buildErrorResponse(businessLog, startTime, 400, "签名验证失败，参数可能被篡改");
            }

            // 验证基本参数
            validateRequest(request);

            // ===== Step 4: 检查Caffeine缓存（使用 openId 作为Key） =====
            BaZiAnalysisResponse cachedResponse = cacheService.get(openId);
            
            if (cachedResponse != null) {
                // 缓存命中，直接返回
                System.out.println("缓存命中: " + openId);
                businessLog.setCacheHit(true);
                businessLog.setAiAnalysis(objectMapper.writeValueAsString(cachedResponse.getAiAnalysis()));
                
                return buildSuccessResponseWithoutToken(businessLog, startTime, cachedResponse);
            }

            businessLog.setCacheHit(false);

            // ===== Step 5: 计算八字（包含大运、流年） =====
            BaZiResult baZiResult = baZiService.calculate(request);
            businessLog.setBaziResult(objectMapper.writeValueAsString(baZiResult));

            // ===== Step 6: 使用 Gemini AI 分析（返回JSON格式） =====
            Object aiAnalysis = geminiService.analyzeBaZi(baZiResult);
            businessLog.setAiAnalysis(objectMapper.writeValueAsString(aiAnalysis));

            // ===== Step 7: 构建响应数据（仅包含AI分析结果） =====
            BaZiAnalysisResponse responseData = new BaZiAnalysisResponse(aiAnalysis);

            // ===== Step 8: 将结果存入Caffeine缓存，过期时间3天 =====
            cacheService.put(openId, responseData);

            // ===== Step 9: 返回JSON格式（不再返回Token） =====
            return buildSuccessResponseWithoutToken(businessLog, startTime, responseData);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse(businessLog, startTime, 400, e.getMessage());
        } catch (Exception e) {
            System.err.println("服务器错误: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(businessLog, startTime, 500, "服务器内部错误: " + e.getMessage());
        }
    }



    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheStats", cacheService.getCacheStats());
        stats.put("logDirectory", businessLogService.getLogDirectoryPath());
        return ResponseEntity.ok(stats);
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
     * 清空所有缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        cacheService.invalidateAll();
        Map<String, Object> result = new HashMap<>();
        result.put("message", "所有缓存已清空");
        result.put("stats", cacheService.getCacheStats());
        return ResponseEntity.ok(result);
    }

    /**
     * 构建成功响应并记录业务日志
     */
    private ResponseEntity<ApiResponse<BaZiAnalysisResponse>> buildSuccessResponseWithoutToken(
            BusinessLog businessLog, long startTime, BaZiAnalysisResponse data) {
        
        long processingTime = System.currentTimeMillis() - startTime;
        businessLog.setResponseCode(200);
        businessLog.setResponseMessage("success");
        businessLog.setProcessingTime(processingTime);
        
        // 记录业务日志
        businessLogService.log(businessLog);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 构建错误响应并记录业务日志
     */
    private ResponseEntity<ApiResponse<BaZiAnalysisResponse>> buildErrorResponse(
            BusinessLog businessLog, long startTime, int code, String message) {
        
        long processingTime = System.currentTimeMillis() - startTime;
        businessLog.setResponseCode(code);
        businessLog.setResponseMessage(message);
        businessLog.setProcessingTime(processingTime);
        
        // 记录业务日志
        businessLogService.log(businessLog);
        
        return ResponseEntity.ok(ApiResponse.error(code, message));
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
