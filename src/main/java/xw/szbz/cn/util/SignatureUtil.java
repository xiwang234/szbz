package xw.szbz.cn.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名验证工具类
 * 用于验证请求参数的签名，防止参数被篡改
 */
@Component
public class SignatureUtil {

    private static final String SIGN_KEY = "szbz-api-sign-key-2024";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成签名
     *
     * @param params    请求参数
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    public String generateSignature(Map<String, Object> params, long timestamp) {
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
            sb.append("key=").append(SIGN_KEY);
            
            // MD5加密
            return md5(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 验证签名
     *
     * @param params    请求参数
     * @param timestamp 时间戳
     * @param sign      客户端传来的签名
     * @return 是否验证通过
     */
    public boolean verifySignature(Map<String, Object> params, long timestamp, String sign) {
        String expectedSign = generateSignature(params, timestamp);
        return expectedSign.equals(sign);
    }

    /**
     * 验证时间戳是否在有效期内（2秒内）
     *
     * @param timestamp 时间戳（毫秒）
     * @return 是否有效
     */
    public boolean validateTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long diff = Math.abs(currentTime - timestamp);
        return diff <= 2000; // 2秒内有效
    }

    /**
     * MD5加密
     *
     * @param input 输入字符串
     * @return MD5加密后的十六进制字符串
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
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

    /**
     * 将对象转换为Map
     *
     * @param obj 对象
     * @return Map
     */
    public Map<String, Object> objectToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }
}
