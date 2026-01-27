package xw.szbz.cn.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xw.szbz.cn.filter.RepeatableReadHttpServletRequest;
import xw.szbz.cn.model.ApiResponse;
import xw.szbz.cn.util.SignatureUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 签名验证拦截器
 * 用于验证API请求的签名，防止参数篡改和重放攻击
 */
@Component
public class SignatureInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SignatureInterceptor.class);

    // 请求头名称
    private static final String HEADER_TIMESTAMP = "X-Sign-Timestamp";
    private static final String HEADER_NONCE = "X-Sign-Nonce";
    private static final String HEADER_SIGN = "X-Sign";

    private final SignatureUtil signatureUtil;
    private final ObjectMapper objectMapper;

    @Value("${api.sign.secret:szbz-api-sign-key-2026}")
    private String signSecret;

    @Value("${api.sign.expiration:300000}")
    private Long signExpiration;

    public SignatureInterceptor(SignatureUtil signatureUtil, ObjectMapper objectMapper) {
        this.signatureUtil = signatureUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestUri = request.getRequestURI();

        // 排除不需要签名验证的接口
        if (isExcludedPath(requestUri)) {
            return true;
        }

        try {
            // ========== 阶段 1：基础校验 ==========

            // 1. 从请求头中提取参数
            String timestamp = request.getHeader(HEADER_TIMESTAMP);
            String nonce = request.getHeader(HEADER_NONCE);
            String sign = request.getHeader(HEADER_SIGN);

            // 2. 检查参数是否缺失
            if (timestamp == null || timestamp.isEmpty()) {
                logger.warn("签名验证失败: timestamp 缺失, URI: {}", requestUri);
                return sendErrorResponse(response, "缺少时间戳参数");
            }
            if (nonce == null || nonce.isEmpty()) {
                logger.warn("签名验证失败: nonce 缺失, URI: {}", requestUri);
                return sendErrorResponse(response, "缺少随机串参数");
            }
            if (sign == null || sign.isEmpty()) {
                logger.warn("签名验证失败: sign 缺失, URI: {}", requestUri);
                return sendErrorResponse(response, "缺少签名参数");
            }

            // 3. 验证 timestamp 格式（必须是数字）
            long timestampValue;
            try {
                timestampValue = Long.parseLong(timestamp);
            } catch (NumberFormatException e) {
                logger.warn("签名验证失败: timestamp 格式错误, timestamp: {}, URI: {}", timestamp, requestUri);
                return sendErrorResponse(response, "时间戳格式错误");
            }

            // 4. 验证 nonce 格式（必须是32位）
            if (!signatureUtil.isNonceValid(nonce)) {
                logger.warn("签名验证失败: nonce 格式错误, nonce: {}, URI: {}", nonce, requestUri);
                return sendErrorResponse(response, "随机串格式错误");
            }

            // 5. 提取所有业务参数
            Map<String, String> params;
            try {
                params = extractParams(request);
            } catch (IOException e) {
                logger.error("签名验证失败: 提取参数异常, URI: {}", requestUri, e);
                return sendErrorResponse(response, "参数解析失败");
            }

            // ========== 阶段 2：防重放因子有效性校验 ==========

            // 6. 验证时间戳有效性（5分钟内）
            if (!signatureUtil.isTimestampValid(timestampValue, signExpiration)) {
                logger.warn("签名验证失败: timestamp 过期, timestamp: {}, URI: {}", timestamp, requestUri);
                return sendErrorResponse(response, "请求已过期");
            }

            // ========== 阶段 3：签名一致性校验 ==========

            // 7. 生成服务端签名
            String serverSign = signatureUtil.generateSignature(params, timestamp, nonce, signSecret);

            // 8. 对比签名
            if (!serverSign.equals(sign)) {
                logger.warn("签名验证失败: 签名不一致, clientSign: {}, serverSign: {}, URI: {}",
                        sign, serverSign, requestUri);
                return sendErrorResponse(response, "签名验证失败");
            }

            // 签名验证通过
            logger.debug("签名验证通过, URI: {}", requestUri);
            return true;

        } catch (Exception e) {
            logger.error("签名验证异常, URI: {}", requestUri, e);
            return sendErrorResponse(response, "签名验证异常");
        }
    }

    /**
     * 判断是否是排除的路径
     */
    private boolean isExcludedPath(String requestUri) {
        // 排除 /api/web-auth/register 接口
        if ("/api/web-auth/register".equals(requestUri)) {
            return true;
        }

        // 排除 /api/bazi/* 的所有接口
        if (requestUri.startsWith("/api/bazi/")) {
            return true;
        }

        return false;
    }

    /**
     * 提取请求参数
     */
    private Map<String, String> extractParams(HttpServletRequest request) throws IOException {
        // 如果是可重复读取的请求，使用包装类
        if (request instanceof RepeatableReadHttpServletRequest) {
            return signatureUtil.extractAllParams(request);
        }

        // 否则直接提取（可能无法读取 Body）
        return signatureUtil.extractAllParams(request);
    }

    /**
     * 发送错误响应
     */
    private boolean sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<String> apiResponse = ApiResponse.error(message);
        String json = objectMapper.writeValueAsString(apiResponse);

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();

        return false;
    }
}
