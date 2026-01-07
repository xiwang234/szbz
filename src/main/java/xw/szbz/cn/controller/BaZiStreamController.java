package xw.szbz.cn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import xw.szbz.cn.entity.WenJi;
import xw.szbz.cn.model.BusinessLog;
import xw.szbz.cn.model.LiuRenRequest;
import xw.szbz.cn.repository.WenJiRepository;
import xw.szbz.cn.service.BusinessLogService;
import xw.szbz.cn.service.GeminiService;
import xw.szbz.cn.service.LiuRenService;
import xw.szbz.cn.util.JwtUtil;
import xw.szbz.cn.util.PromptTemplateUtil;
import xw.szbz.cn.util.SignatureUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流式SSE控制器
 * 专门处理问吉接口的流式响应
 */
@RestController
@RequestMapping("/api/bazi")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BaZiStreamController {

    private static final Logger logger = LoggerFactory.getLogger(BaZiStreamController.class);

    private final JwtUtil jwtUtil;
    private final SignatureUtil signatureUtil;
    private final LiuRenService liuRenService;
    private final GeminiService geminiService;
    private final PromptTemplateUtil promptTemplateUtil;
    private final WenJiRepository wenJiRepository;
    private final BusinessLogService businessLogService;

    // 异步执行器，避免阻塞主线程
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate.limit.wenji.daily:5}")
    private int wenjiDailyLimit;

    @Autowired
    public BaZiStreamController(
            JwtUtil jwtUtil,
            SignatureUtil signatureUtil,
            LiuRenService liuRenService,
            GeminiService geminiService,
            PromptTemplateUtil promptTemplateUtil,
            WenJiRepository wenJiRepository,
            BusinessLogService businessLogService) {
        this.jwtUtil = jwtUtil;
        this.signatureUtil = signatureUtil;
        this.liuRenService = liuRenService;
        this.geminiService = geminiService;
        this.promptTemplateUtil = promptTemplateUtil;
        this.wenJiRepository = wenJiRepository;
        this.businessLogService = businessLogService;
    }

    /**
     * 问吉流式接口 - 使用 SSE 推送
     * 
     * @param request 问吉请求参数
     * @param token JWT Token
     * @param timestamp 时间戳
     * @param sign 签名
     * @param httpRequest HTTP请求对象
     * @return SSE 流
     */
    @PostMapping(value = "/wenji-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter wenjiStream(
            @RequestBody LiuRenRequest request,
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Timestamp", required = false) Long timestamp,
            @RequestHeader(value = "X-Sign", required = false) String sign,
            HttpServletRequest httpRequest) {

        logger.info("[SSE] 收到问吉流式请求");
        
        // SSE 超时设置（3分钟，考虑 Gemini API 可能需要较长时间）
        SseEmitter emitter = new SseEmitter(180_000L);
        
        long startTime = System.currentTimeMillis();
        BusinessLog businessLog = new BusinessLog();
        
        // 异步处理，避免阻塞
        executorService.execute(() -> {
            String openId = "";
            
            try {
                // 记录请求信息
                businessLog.setRequestIp(getClientIp(httpRequest));
                businessLog.setUserAgent(httpRequest.getHeader("User-Agent"));

                // ===== Step 1: 验证JWT Token =====
                if (token == null || token.isEmpty()) {
                    sendError(emitter, "未提供认证Token");
                    return;
                }
                
                // 处理 Bearer 前缀，使用新变量避免修改参数
                final String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
                
                if (!jwtUtil.validateToken(actualToken)) {
                    sendError(emitter, "Token无效或已过期");
                    return;
                }
                
                try {
                    openId = jwtUtil.getOpenIdFromToken(actualToken);
                    businessLog.setOpenId(openId);
                    logger.info("[SSE] Token验证成功，OpenId: {}", openId);
                } catch (Exception e) {
                    sendError(emitter, "Token解析失败");
                    return;
                }

                // ===== Step 2: 验证时间戳 =====
                if (timestamp == null) {
                    sendError(emitter, "缺少时间戳参数");
                    return;
                }
                if (!signatureUtil.validateTimestamp(timestamp)) {
                    sendError(emitter, "请求已过期，时间戳超过2秒");
                    return;
                }

                // ===== Step 3: 验证签名 =====
                if (sign == null || sign.isEmpty()) {
                    sendError(emitter, "缺少签名参数");
                    return;
                }
                Map<String, Object> params = signatureUtil.objectToMap(request);
                if (!signatureUtil.verifySignature(params, timestamp, sign)) {
                    sendError(emitter, "签名验证失败，参数可能被篡改");
                    return;
                }

                // ===== Step 4: 限流检查 =====
                if (rateLimitEnabled) {
                    long todayCount = getTodaySubmitCount(openId);
                    if (todayCount >= wenjiDailyLimit) {
                        String errorMsg = String.format("今日提交次数已达上限（%d/%d），请明天再试", todayCount, wenjiDailyLimit);
                        sendError(emitter, errorMsg);
                        return;
                    }
                    logger.info("[SSE] 限流检查通过，今日已提交: {}/{}", todayCount, wenjiDailyLimit);
                }

                // ===== Step 5: 参数验证 =====
                if (request.getQuestion() == null || request.getQuestion().isEmpty()) {
                    sendError(emitter, "占问事项不能为空");
                    return;
                }
                if (request.getBirthYear() == null) {
                    sendError(emitter, "出生年份不能为空");
                    return;
                }
                if (request.getGender() == null || request.getGender().isEmpty()) {
                    sendError(emitter, "性别不能为空");
                    return;
                }
                if (!request.getGender().equals("male") && !request.getGender().equals("female")) {
                    sendError(emitter, "性别只能是'male'或'female'");
                    return;
                }

                // ===== Step 6: 生成课传信息 =====
                String courseInfo = liuRenService.generateCourseInfo();
                logger.info("[SSE] 生成课传信息: {}", courseInfo);

                // 推送课传信息
                sendEvent(emitter, "courseInfo", courseInfo);

                // ===== Step 7: 转换干支年份 =====
                String ganZhiYear = liuRenService.convertBirthYearToGanZhi(request.getBirthYear());
                logger.info("[SSE] 干支年份: {}", ganZhiYear);

                // ===== Step 8: 生成出生信息 =====
                String birthInfo = liuRenService.generateBirthInfo(request.getBirthYear(), request.getGender());
                logger.info("[SSE] 出生信息: {}", birthInfo);

                // 推送出生信息
                sendEvent(emitter, "birthInfo", birthInfo);

                // ===== Step 9: 渲染提示词模板 =====
                String prompt = promptTemplateUtil.renderLiuRenTemplate(
                    courseInfo,
                    request.getQuestion(),
                    request.getBackground(),
                    birthInfo
                );

                // ===== Step 10: 调用Gemini AI进行流式预测 =====
                logger.info("[SSE] 开始调用 Gemini 流式 API");
                
                StringBuilder fullPrediction = new StringBuilder();
                
                // 调用流式生成方法
                geminiService.generateContentStream(prompt, (chunk) -> {
                    try {
                        // 推送每个文本片段
                        sendEvent(emitter, "chunk", chunk);
                        fullPrediction.append(chunk);
                        logger.debug("[SSE] 推送文本片段: {} 字符", chunk.length());
                    } catch (Exception e) {
                        logger.error("[SSE] 推送文本片段失败", e);
                    }
                });

                // ===== Step 11: 保存到问吉表 =====
                WenJi wenJi = new WenJi(
                    openId,
                    request.getQuestion(),
                    request.getBackground(),
                    request.getBirthYear(),
                    request.getGender(),
                    fullPrediction.toString(),
                    System.currentTimeMillis()
                );
                wenJiRepository.save(wenJi);
                logger.info("[SSE] 问吉记录已保存到数据库");

                // ===== Step 12: 发送完成事件 =====
                sendEvent(emitter, "done", "完成");
                
                // 记录业务日志
                businessLog.setResponseCode(200);
                businessLog.setResponseMessage("success");
                businessLog.setProcessingTime(System.currentTimeMillis() - startTime);
                businessLogService.log(businessLog);

                // 完成并关闭连接
                emitter.complete();
                logger.info("[SSE] 流式响应完成，总耗时: {} ms", System.currentTimeMillis() - startTime);

            } catch (Exception e) {
                logger.error("[SSE] 处理流式请求异常", e);
                sendError(emitter, "服务器内部错误: " + e.getMessage());
                
                // 记录错误日志
                businessLog.setResponseCode(500);
                businessLog.setResponseMessage("error: " + e.getMessage());
                businessLog.setProcessingTime(System.currentTimeMillis() - startTime);
                businessLogService.log(businessLog);
            }
        });

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            logger.warn("[SSE] 连接超时");
            emitter.completeWithError(new RuntimeException("SSE连接超时"));
        });

        emitter.onError((e) -> {
            logger.error("[SSE] 连接错误", e);
        });

        return emitter;
    }

    /**
     * 发送 SSE 事件
     */
    private void sendEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data));
        } catch (IOException e) {
            logger.error("[SSE] 发送事件失败: {}", event, e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送错误消息并关闭连接
     */
    private void sendError(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
            emitter.complete();
        } catch (IOException e) {
            logger.error("[SSE] 发送错误消息失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 获取今日提交次数
     */
    private long getTodaySubmitCount(String openId) {
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % 86400000) - 28800000;
        long endOfDay = startOfDay + 86400000;
        return wenJiRepository.countByOpenIdAndCreateTimeBetween(openId, startOfDay, endOfDay);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
