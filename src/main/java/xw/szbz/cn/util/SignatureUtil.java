package xw.szbz.cn.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API 签名验证工具类
 */
@Component
public class SignatureUtil {

    private final PasswordHashUtil passwordHashUtil;
    private final ObjectMapper objectMapper;

    public SignatureUtil(PasswordHashUtil passwordHashUtil, ObjectMapper objectMapper) {
        this.passwordHashUtil = passwordHashUtil;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成签名
     *
     * @param params 业务参数
     * @param timestamp 时间戳
     * @param nonce 随机串
     * @param secret 签名密钥
     * @return 签名字符串
     */
    public String generateSignature(Map<String, String> params, String timestamp, String nonce, String secret) {
        // 1. 创建 TreeMap 自动按 key 排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 添加 timestamp 和 nonce
        System.out.println("Params before adding timestamp and nonce: " + params);
        sortedParams.put("timestamp", timestamp);
        sortedParams.put("nonce", nonce);
        System.out.println(timestamp);
        System.out.println(nonce);
        // 3. 过滤空值参数，拼接参数字符串
        String paramString = sortedParams.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
        System.out.println("Param String: " + paramString);
        // 4. 拼接签名源字符串：参数字符串 + 密钥
        String signSource = paramString + secret;
        System.out.println("Sign Source: " + signSource);
        // 5. SHA-256 加密
        return passwordHashUtil.sha256Hash(signSource);
    }

    /**
     * 从请求中提取所有参数（URL路径参数 + Query参数 + Body参数）
     *
     * @param request HTTP请求
     * @return 参数Map
     */
    public Map<String, String> extractAllParams(HttpServletRequest request) throws IOException {
        TreeMap<String, String> params = new TreeMap<>();

        // 1. 提取 Query 参数
        Map<String, String[]> queryParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > 0) {
                params.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        // 2. 提取 Body 参数（仅对 POST/PUT/PATCH 请求）
        String method = request.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // 读取 Body（需要使用可重复读取的包装类）
                String body = getRequestBody(request);
                if (body != null && !body.isEmpty()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> bodyParams = objectMapper.readValue(body, Map.class);
                        flattenMap(bodyParams, "", params);
                    } catch (Exception e) {
                        // 忽略 JSON 解析错误
                    }
                }
            }
        }

        // 3. 移除空值参数
        params.entrySet().removeIf(entry ->
            entry.getValue() == null || entry.getValue().isEmpty() || "null".equals(entry.getValue())
        );

        return params;
    }

    /**
     * 展平嵌套的 Map（支持嵌套对象）
     */
    private void flattenMap(Map<String, Object> source, String prefix, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMap(nestedMap, key, target);
            } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                target.put(key, value.toString());
            } else {
                // 对于复杂对象，转换为 JSON 字符串
                try {
                    target.put(key, objectMapper.writeValueAsString(value));
                } catch (Exception e) {
                    target.put(key, value.toString());
                }
            }
        }
    }

    /**
     * 读取请求 Body
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        return reader.lines().collect(Collectors.joining());
    }

    /**
     * 验证时间戳是否在有效期内
     *
     * @param timestamp 时间戳（毫秒）
     * @param expirationMillis 有效期（毫秒）
     * @return 是否有效
     */
    public boolean isTimestampValid(long timestamp, long expirationMillis) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp) <= expirationMillis;
    }

    /**
     * 验证 nonce 格式（32位字符串）
     *
     * @param nonce 随机串
     * @return 是否有效
     */
    public boolean isNonceValid(String nonce) {
        return nonce != null && nonce.length() == 32;
    }

    // ========== 以下是兼容旧版本的方法（用于 BaZiController）==========

    /**
     * 验证时间戳是否在有效期内（旧版本方法，2秒内有效）
     * @deprecated 建议使用 isTimestampValid(long timestamp, long expirationMillis)
     */
    @Deprecated
    public boolean validateTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = Math.abs(currentTime - timestamp);
        return diff <= 2000; // 2秒内有效
    }

    /**
     * 将对象转换为Map（旧版本方法）
     * @deprecated 建议使用 extractAllParams(HttpServletRequest request)
     */
    @Deprecated
    public Map<String, Object> objectToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    /**
     * 验证签名（旧版本方法，使用MD5）
     * @deprecated 建议使用新的签名验证方法
     */
    @Deprecated
    public boolean verifySignature(Map<String, Object> params, long timestamp, String sign) {
        String expectedSign = generateSignatureOld(params, timestamp);
        return expectedSign.equals(sign);
    }

    /**
     * 生成签名（旧版本方法，使用MD5）
     * @deprecated 建议使用 generateSignature(Map<String, String> params, String timestamp, String nonce, String secret)
     */
    @Deprecated
    private String generateSignatureOld(Map<String, Object> params, long timestamp) {
        try {
            // 使用TreeMap自动按key排序
            TreeMap<String, Object> sortedParams = new TreeMap<>(params);
            sortedParams.put("timestamp", timestamp);

            // 拼接参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            sb.append("key=").append("szbz-api-sign-key-2024");

            // MD5加密
            return md5(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * MD5加密（旧版本方法）
     * @deprecated SHA-256 更安全
     */
    @Deprecated
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
