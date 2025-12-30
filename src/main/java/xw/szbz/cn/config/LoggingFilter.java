package xw.szbz.cn.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 接口请求响应日志过滤器
 * 记录所有接口的请求参数、响应结果和耗时
 */
@Component
@Order(1)
public class LoggingFilter implements Filter {

    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("ACCESS_LOGGER");
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 类型转换
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 包装请求和响应以便读取内容
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // 执行请求
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 记录请求响应日志
            logRequestResponse(requestWrapper, responseWrapper, duration);

            // 将响应内容复制回原始响应
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 记录请求响应日志
     */
    private void logRequestResponse(ContentCachingRequestWrapper request,
                                     ContentCachingResponseWrapper response,
                                     long duration) {
        try {
            Map<String, Object> logData = new HashMap<>();

            // 基本信息
            logData.put("timestamp", System.currentTimeMillis());
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            logData.put("queryString", request.getQueryString());

            // 请求信息
            Map<String, Object> requestInfo = new HashMap<>();

            // 获取请求参数（避免读取大文件）
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                byte[] content = request.getContentAsByteArray();
                if (content.length > 0 && content.length < 10240) { // 小于10KB
                    String body = new String(content, StandardCharsets.UTF_8);
                    requestInfo.put("body", body);
                }
            } else {
                requestInfo.put("parameters", request.getParameterMap());
            }

            logData.put("request", requestInfo);

            // 响应信息
            Map<String, Object> responseInfo = new HashMap<>();
            responseInfo.put("status", response.getStatus());
            responseInfo.put("duration", duration + "ms");

            // 获取响应内容（避免读取大文件）
            byte[] responseContent = response.getContentAsByteArray();
            if (responseContent.length > 0 && responseContent.length < 10240) { // 小于10KB
                String responseBody = new String(responseContent, StandardCharsets.UTF_8);
                // 尝试格式化 JSON
                try {
                    Object json = objectMapper.readValue(responseBody, Object.class);
                    responseInfo.put("body", json);
                } catch (Exception e) {
                    responseInfo.put("body", responseBody);
                }
            } else {
                responseInfo.put("bodySize", responseContent.length + " bytes");
            }

            logData.put("response", responseInfo);

            // 记录日志（JSON格式）
            String logJson = objectMapper.writeValueAsString(logData);
            ACCESS_LOGGER.info(logJson);

        } catch (Exception e) {
            logger.error("记录访问日志失败", e);
        }
    }

}
