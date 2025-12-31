package xw.szbz.cn.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 安全过滤器 - 拦截常见的恶意扫描请求
 * Order(0) 确保在 LoggingFilter 之前执行，避免记录恶意请求日志
 */
@Component
@Order(0)
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    // 恶意路径关键词黑名单（常见的漏洞扫描路径）
    private static final List<String> MALICIOUS_PATH_PATTERNS = Arrays.asList(
        // PHP 相关漏洞扫描
        "phpunit",
        "eval-stdin.php",
        "vendor/",
        ".php",
        "wp-admin",
        "wp-login",
        "wp-content",
        "wordpress",

        // 其他常见漏洞扫描
        ".env",
        ".git",
        ".aws",
        "adminer",
        "phpmyadmin",
        "mysql",
        "sql",
        "admin.php",
        "config.php",
        "install.php",
        "setup.php",

        // 路径遍历尝试
        "../",
        "..\\",

        // Shell 和命令注入
        ".sh",
        ".cgi",
        ".pl"
    );

    // 恶意参数关键词黑名单
    private static final List<String> MALICIOUS_PARAM_PATTERNS = Arrays.asList(
        "<?",           // PHP 标签
        "?>",
        "eval(",        // 代码执行
        "base64_decode",
        "system(",
        "exec(",
        "passthru(",
        "shell_exec",
        "<script",      // XSS 尝试
        "javascript:",
        "onerror=",
        "onload="
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        // 检查 URI 是否包含恶意路径模式
        if (containsMaliciousPattern(uri, MALICIOUS_PATH_PATTERNS)) {
            logger.warn("阻止恶意路径访问: {} from IP: {}", uri, getClientIP(httpRequest));
            sendSecurityResponse(httpResponse);
            return;
        }

        // 检查查询参数是否包含恶意模式
        if (queryString != null && containsMaliciousPattern(queryString, MALICIOUS_PARAM_PATTERNS)) {
            logger.warn("阻止恶意参数请求: {} from IP: {}", queryString, getClientIP(httpRequest));
            sendSecurityResponse(httpResponse);
            return;
        }

        // 检查所有请求参数值
        for (String[] paramValues : httpRequest.getParameterMap().values()) {
            String value = String.join(",", paramValues);
            if (containsMaliciousPattern(value, MALICIOUS_PARAM_PATTERNS)) {
                logger.warn("阻止恶意参数值: {} from IP: {}", value, getClientIP(httpRequest));
                sendSecurityResponse(httpResponse);
                return;
            }
        }

        // 继续正常请求处理
        chain.doFilter(request, response);
    }

    /**
     * 检查文本是否包含恶意模式
     */
    private boolean containsMaliciousPattern(String text, List<String> patterns) {
        if (text == null) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String pattern : patterns) {
            if (lowerText.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送安全拦截响应（403 Forbidden，不记录到 access.log）
     */
    private void sendSecurityResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}");
    }

    /**
     * 获取客户端真实 IP 地址（考虑代理和负载均衡）
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个 IP（经过多个代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
