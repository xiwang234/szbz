package xw.szbz.cn.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import xw.szbz.cn.entity.JiTu;
import xw.szbz.cn.entity.User;
import xw.szbz.cn.entity.WenJi;
import xw.szbz.cn.exception.ServiceException;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.model.BaZiAnalysisResponse;
import xw.szbz.cn.model.BaZiRequest;
import xw.szbz.cn.model.BaZiResult;
import xw.szbz.cn.model.BusinessLog;
import xw.szbz.cn.model.LiuRenRequest;
import xw.szbz.cn.model.LoginRequest;
import xw.szbz.cn.model.LoginResponse;
import xw.szbz.cn.repository.JiTuRepository;
import xw.szbz.cn.repository.UserRepository;
import xw.szbz.cn.repository.WenJiRepository;
import xw.szbz.cn.service.BaZiService;
import xw.szbz.cn.service.BusinessLogService;
import xw.szbz.cn.service.GeminiService;
import xw.szbz.cn.service.LiuRenService;
import xw.szbz.cn.service.WeChatService;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.PromptTemplateUtil;
import xw.szbz.cn.util.SignatureUtil;

/**
 * 四柱八字API控制器
 */
@RestController
@RequestMapping("/api/bazi")
public class BaZiController {

    private static final Logger logger = LoggerFactory.getLogger(BaZiController.class);
    
    private final BaZiService baZiService;
    private final GeminiService geminiService;
    private final WeChatService weChatService;
    private final JwtUtil jwtUtil;
    private final SignatureUtil signatureUtil;
    private final BusinessLogService businessLogService;
    private final ObjectMapper objectMapper;
    private final PromptTemplateUtil promptTemplateUtil;
    private final LiuRenService liuRenService;
    private final UserRepository userRepository;
    private final WenJiRepository wenJiRepository;
    private final JiTuRepository jiTuRepository;
    
    // 限流配置
    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;
    
    @Value("${rate.limit.wenji.daily:5}")
    private int wenjiDailyLimit;
    
    @Value("${rate.limit.jitu.daily:5}")
    private int jituDailyLimit;

    @Autowired
    public BaZiController(BaZiService baZiService,
                          GeminiService geminiService,
                          WeChatService weChatService,
                          JwtUtil jwtUtil,
                          SignatureUtil signatureUtil,
                          BusinessLogService businessLogService,
                          ObjectMapper objectMapper,
                          PromptTemplateUtil promptTemplateUtil,
                          LiuRenService liuRenService,
                          UserRepository userRepository,
                          WenJiRepository wenJiRepository,
                          JiTuRepository jiTuRepository) {
        this.baZiService = baZiService;
        this.geminiService = geminiService;
        this.weChatService = weChatService;
        this.jwtUtil = jwtUtil;
        this.signatureUtil = signatureUtil;
        this.businessLogService = businessLogService;
        this.objectMapper = objectMapper;
        this.promptTemplateUtil = promptTemplateUtil;
        this.liuRenService = liuRenService;
        this.userRepository = userRepository;
        this.wenJiRepository = wenJiRepository;
        this.jiTuRepository = jiTuRepository;
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

            // 保存用户信息到数据库（如果不存在）
            if (!userRepository.existsByOpenId(openId)) {
                User user = new User(openId, System.currentTimeMillis());
                userRepository.save(user);
                System.out.println("新用户已保存到数据库: " + openId);
            }

            // 构建响应（不返回openId，保护用户隐私）
            LoginResponse response = new LoginResponse(token, expiresAt);
            
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            System.err.println("登录服务器错误: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }

    /**
     * 六壬预测接口
     * 根据课传信息、占问事项等生成预测提示词，并调用AI进行分析
     *
     * @param request 六壬预测请求（包含课传信息、占问事项、占问背景、出生年份）
     * @param token JWT Token（Header）
     * @param timestamp 时间戳（Header）
     * @param sign 签名（Header）
     * @param httpRequest HTTP请求对象
     * @return 包含AI预测结果的响应
     */
    @PostMapping("/wenji")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predictLiuRen(
            @RequestBody LiuRenRequest request,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Timestamp", required = false) Long timestamp,
            @RequestHeader(value = "X-Sign", required = false) String sign,
            HttpServletRequest httpRequest) {

        long startTime = System.currentTimeMillis();
        BusinessLog businessLog = new BusinessLog();
        String openId = "";
        
        try {
            // 记录请求信息
            businessLog.setRequestIp(getClientIp(httpRequest));
            businessLog.setUserAgent(httpRequest.getHeader("User-Agent"));

            // ===== Step 1: 验证JWT Token =====
            if (token == null || token.isEmpty()) {
                return buildLiuRenErrorResponse(businessLog, startTime, 401, "未提供认证Token");
            }
            
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (!jwtUtil.validateToken(token)) {
                return buildLiuRenErrorResponse(businessLog, startTime, 401, "Token无效或已过期");
            }
            
            try {
                openId = jwtUtil.getOpenIdFromToken(token);
                businessLog.setOpenId(openId);
                System.out.println("Token验证成功，OpenId: " + openId);
            } catch (Exception e) {
                return buildLiuRenErrorResponse(businessLog, startTime, 401, "Token解析失败");
            }

            // ===== Step 2: 验证时间戳 =====
            if (timestamp == null) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "缺少时间戳参数");
            }
            if (!signatureUtil.validateTimestamp(timestamp)) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "请求已过期，时间戳超过2秒");
            }

            // ===== Step 3: 验证签名 =====
            if (sign == null || sign.isEmpty()) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "缺少签名参数");
            }
            Map<String, Object> params = signatureUtil.objectToMap(request);
            if (!signatureUtil.verifySignature(params, timestamp, sign)) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "签名验证失败，参数可能被篡改");
            }

            // ===== Step 4: 限流检查（问吉接口） =====
            if (rateLimitEnabled) {
                long todayCount = getTodaySubmitCount(openId, wenJiRepository);
                if (todayCount >= wenjiDailyLimit) {
                    String errorMsg = String.format("今日提交次数已达上限（%d/%d），请明天再试", todayCount, wenjiDailyLimit);
                    return buildLiuRenErrorResponse(businessLog, startTime, 429, errorMsg);
                }
                System.out.println(String.format("问吉限流检查通过，今日已提交: %d/%d", todayCount, wenjiDailyLimit));
            }

            // ===== Step 4: 验证参数 =====
            if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "占问事项不能为空");
            }
            if (request.getBirthYear() == null) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "出生年份不能为空");
            }
            if (request.getGender() == null || request.getGender().isEmpty()) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "性别不能为空");
            }
            if (!request.getGender().equals("male") && !request.getGender().equals("female")) {
                return buildLiuRenErrorResponse(businessLog, startTime, 400, "性别只能是'male'或'female'");
            }

            // ===== Step 6: 生成课传信息（根据当前时间排出农历四柱及大六壬天将） =====
            String courseInfo = liuRenService.generateCourseInfo();
            System.out.println("生成课传信息: " + courseInfo);

            // ===== Step 7: 将出生年份转换为干支年份 =====
            String ganZhiYear = liuRenService.convertBirthYearToGanZhi(request.getBirthYear());
            System.out.println("干支年份: " + ganZhiYear);

            // ===== Step 8: 组合干支信息和性别生成birthInfo =====
            String birthInfo = liuRenService.generateBirthInfo(request.getBirthYear(), request.getGender());
            System.out.println("出生信息: " + birthInfo);

            // ===== Step 9: 渲染提示词模板 =====
            String prompt = promptTemplateUtil.renderLiuRenTemplate(
                courseInfo,
                request.getQuestion(),
                request.getBackground(),
                birthInfo
            );

            logger.info("六壬预测提示词: " + prompt);

            // ===== Step 10: 调用Gemini AI进行预测 =====
            String aiPrediction = geminiService.generateContent(prompt);
            logger.info("六壬预测结果: " + aiPrediction);

            // ===== Step 11: 保存到问吉表 =====
            WenJi wenJi = new WenJi(
                openId,
                request.getQuestion(),
                request.getBackground(),
                request.getBirthYear(),
                request.getGender(),
                aiPrediction,
                System.currentTimeMillis()
            );
            wenJiRepository.save(wenJi);
            System.out.println("问吉记录已保存到数据库");

            // ===== Step 12: 构建响应 =====
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("prediction", aiPrediction);
            responseData.put("courseInfo", courseInfo);
            responseData.put("question", request.getQuestion());
            responseData.put("birthInfo", birthInfo);

            // 记录业务日志
            businessLog.setResponseCode(200);
            businessLog.setResponseMessage("success");
            businessLog.setProcessingTime(System.currentTimeMillis() - startTime);
            businessLogService.log(businessLog);

            logger.info("六壬预测响应: " + responseData);

            return ResponseEntity.ok(ApiResponse.success(responseData));

        } catch (ServiceException e) {
            // 业务异常，返回用户友好消息
            logger.error("业务异常: {}", e.getUserMessage());
            return buildLiuRenErrorResponse(businessLog, startTime, e.getStatusCode(), e.getUserMessage());
        } catch (Exception e) {
            logger.error("六壬预测服务器错误", e);
            return buildLiuRenErrorResponse(businessLog, startTime, 500, "服务暂时不可用，请稍后重试");
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
        String openId = "";
        
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

            // ===== Step 4: 限流检查（吉途接口） =====
            if (rateLimitEnabled) {
                long todayCount = getTodaySubmitCount(openId, jiTuRepository);
                if (todayCount >= jituDailyLimit) {
                    String errorMsg = String.format("今日提交次数已达上限（%d/%d），请明天再来", todayCount, jituDailyLimit);
                    return buildErrorResponse(businessLog, startTime, 429, errorMsg);
                }
                System.out.println(String.format("吉途限流检查通过，今日已提交: %d/%d", todayCount, jituDailyLimit));
            }

            // ===== Step 5: 查询吉途表缓存（根据gender、year、month、day、hour） =====
            Optional<JiTu> cachedJiTu = jiTuRepository.findFirstByGenderAndYearAndMonthAndDayAndHourOrderByCreateTimeDesc(
                request.getGender(),
                request.getYear(),
                request.getMonth(),
                request.getDay(),
                request.getHour()
            );

            Object aiAnalysis;
            
            if (cachedJiTu.isPresent()) {
                // 找到缓存数据，直接返回
                System.out.println("从数据库缓存中获取吉途数据，性别: " + request.getGender() + 
                    ", 出生日期: " + request.getYear() + "-" + request.getMonth() + "-" + 
                    request.getDay() + " " + request.getHour() + "时");
                
                String cachedResult = cachedJiTu.get().getDefaultResult();
                aiAnalysis = objectMapper.readValue(cachedResult, Object.class);
                businessLog.setAiAnalysis(cachedResult);
                
            } else {
                // ===== Step 6: 计算八字（包含大运、流年） =====
                BaZiResult baZiResult = baZiService.calculate(request);
                businessLog.setBaziResult(objectMapper.writeValueAsString(baZiResult));
                logger.info("八字计算结果: " + baZiResult);

                // ===== Step 7: 使用 Gemini AI 分析（返回JSON格式） =====
                aiAnalysis = geminiService.analyzeBaZi(baZiResult);
                String aiAnalysisJson = objectMapper.writeValueAsString(aiAnalysis);
                businessLog.setAiAnalysis(aiAnalysisJson);

                // ===== Step 8: 保存到吉途表 =====
                JiTu jiTu = new JiTu(
                    openId,
                    request.getGender(),
                    request.getYear(),
                    request.getMonth(),
                    request.getDay(),
                    request.getHour(),
                    aiAnalysisJson,
                    System.currentTimeMillis()
                );
                jiTuRepository.save(jiTu);
                System.out.println("吉途记录已保存到数据库");
            }

            // ===== Step 9: 构建响应数据（仅包含AI分析结果） =====
            BaZiAnalysisResponse responseData = new BaZiAnalysisResponse(aiAnalysis);

            // ===== Step 10: 返回JSON格式（不再返回Token） =====
            return buildSuccessResponseWithoutToken(businessLog, startTime, responseData);

        } catch (IllegalArgumentException e) {
            return buildErrorResponse(businessLog, startTime, 400, e.getMessage());
        } catch (ServiceException e) {
            // 业务异常，返回用户友好消息
            logger.error("业务异常: {}", e.getUserMessage());
            return buildErrorResponse(businessLog, startTime, e.getStatusCode(), e.getUserMessage());
        } catch (Exception e) {
            logger.error("服务器错误", e);
            return buildErrorResponse(businessLog, startTime, 500, "服务暂时不可用，请稍后重试");
        }
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
     * 构建六壬预测错误响应
     */
    private ResponseEntity<ApiResponse<Map<String, Object>>> buildLiuRenErrorResponse(
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

    /**
     * 获取今日提交次数（通用方法）
     * @param openId 用户openId
     * @param repository Repository接口（WenJiRepository或JiTuRepository）
     * @return 今日提交次数
     */
    private long getTodaySubmitCount(String openId, Object repository) {
        // 获取今日起始和结束时间戳
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        
        // 根据repository类型调用对应的查询方法
        if (repository instanceof WenJiRepository) {
            return ((WenJiRepository) repository).countByOpenIdAndCreateTimeBetween(openId, startOfDay, endOfDay);
        } else if (repository instanceof JiTuRepository) {
            return ((JiTuRepository) repository).countByOpenIdAndCreateTimeBetween(openId, startOfDay, endOfDay);
        }
        return 0;
    }
}
