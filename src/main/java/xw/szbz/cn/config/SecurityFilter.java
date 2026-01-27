package xw.szbz.cn.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 * 
 * 新增功能：
 * 1. RCE 攻击检测（Node.js、Python、Shell 命令注入）
 * 2. HTTP Method 白名单（禁止 PROPFIND、PROPPATCH 等 WebDAV 方法）
 * 3. IP 限流（每分钟最多 60 次请求）
 * 4. 恶意 IP 自动封禁（累计拦截 5 次）
 */
@Component
@Order(0)
public class SecurityFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    // HTTP Method 白名单
    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
    ));

    // IP 限流：每个 IP 的请求计数（每分钟重置）
    private final ConcurrentHashMap<String, AtomicInteger> ipRequestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> ipRequestResetTime = new ConcurrentHashMap<>();
    
    // IP 黑名单：累计拦截 5 次的 IP 自动封禁
    private final ConcurrentHashMap<String, AtomicInteger> ipBlockCounts = new ConcurrentHashMap<>();
    private final Set<String> blockedIps = ConcurrentHashMap.newKeySet();

    @Value("${security.ip.rate.limit:60}")
    private int ipRateLimit; // 每分钟最多请求次数

    @Value("${security.ip.block.threshold:5}")
    private int blockThreshold; // 累计拦截多少次后封禁

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
        "onload=",
        
        // RCE 攻击特征（Node.js）
        "child_process",
        "execSync",
        "require(",
        "process.mainModule",
        "__proto__",
        "constructor.constructor",
        
        // Python RCE
        "__import__",
        "os.system",
        "subprocess",
        
        // Shell 命令注入
        ";wget",
        "|wget",
        ";curl",
        "|curl",
        ";sh",
        "|sh",
        ";bash",
        "|bash",
        "mkfifo",
        "|nc",
        ";nc"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String method = httpRequest.getMethod();
        String clientIp = getClientIP(httpRequest);

        // ===== 防护 1: 检查 IP 是否在黑名单中 =====
        if (blockedIps.contains(clientIp)) {
            logger.error("拦截已封禁 IP 访问: {} from {}", uri, clientIp);
            sendBlockedResponse(httpResponse, "IP 已被封禁");
            return;
        }

        // ===== 防护 2: IP 限流（每分钟最多 60 次请求） =====
        if (!checkIpRateLimit(clientIp)) {
            logger.warn("IP 限流拦截: {} - 超过每分钟 {} 次请求限制", clientIp, ipRateLimit);
            incrementBlockCount(clientIp, "频繁请求");
            sendRateLimitResponse(httpResponse);
            return;
        }

        // ===== 防护 3: HTTP Method 白名单（禁止 WebDAV 方法） =====
        if (!ALLOWED_METHODS.contains(method)) {
            logger.warn("拦截非法 HTTP Method: {} {} from IP: {}", method, uri, clientIp);
            incrementBlockCount(clientIp, "非法 HTTP Method: " + method);
            sendMethodNotAllowedResponse(httpResponse);
            return;
        }

        // ===== 防护 4: 检查 URI 是否包含恶意路径模式 =====
        if (containsMaliciousPattern(uri, MALICIOUS_PATH_PATTERNS)) {
            logger.warn("阻止恶意路径访问: {} from IP: {}", uri, clientIp);
            incrementBlockCount(clientIp, "恶意路径: " + uri);
            sendSecurityResponse(httpResponse);
            return;
        }

        // ===== 防护 5: 检查查询参数是否包含恶意模式（包括 RCE 攻击特征） =====
        if (queryString != null && containsMaliciousPattern(queryString, MALICIOUS_PARAM_PATTERNS)) {
            logger.warn("阻止恶意参数请求: {} from IP: {}", queryString, clientIp);
            incrementBlockCount(clientIp, "恶意查询参数");
            sendSecurityResponse(httpResponse);
            return;
        }

        // ===== 防护 6: 检查所有请求参数值（包括 POST Body 中的 JSON 参数） =====
        for (String[] paramValues : httpRequest.getParameterMap().values()) {
            String value = String.join(",", paramValues);
            if (containsMaliciousPattern(value, MALICIOUS_PARAM_PATTERNS)) {
                logger.error("⚠️ 检测到 RCE 攻击尝试！参数值: {} from IP: {}", 
                    value.length() > 200 ? value.substring(0, 200) + "..." : value, clientIp);
                incrementBlockCount(clientIp, "RCE 攻击尝试");
                sendSecurityResponse(httpResponse);
                return;
            }
        }

        // 继续正常请求处理
        chain.doFilter(request, response);
    }

    /**
     * IP 限流检查（每分钟最多 ipRateLimit 次请求）
     */
    private boolean checkIpRateLimit(String ip) {
        long now = System.currentTimeMillis();
        
        // 获取或初始化该 IP 的重置时间
        Long resetTime = ipRequestResetTime.computeIfAbsent(ip, k -> now + 60000);
        
        // 如果已过期，重置计数器
        if (now > resetTime) {
            ipRequestCounts.put(ip, new AtomicInteger(1));
            ipRequestResetTime.put(ip, now + 60000);
            return true;
        }
        
        // 增加计数并检查是否超限
        AtomicInteger count = ipRequestCounts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= ipRateLimit;
    }

    /**
     * 增加 IP 的拦截计数，累计达到阈值则封禁
     */
    private void incrementBlockCount(String ip, String reason) {
        AtomicInteger count = ipBlockCounts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        if (currentCount >= blockThreshold) {
            blockedIps.add(ip);
            logger.error("🚫 IP {} 已被自动封禁！累计拦截 {} 次，最后原因: {}", ip, currentCount, reason);
        } else {
            logger.warn("IP {} 拦截计数: {}/{} - 原因: {}", ip, currentCount, blockThreshold, reason);
        }
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
     * 发送安全拦截响应（403 Forbidden）
     */
    private void sendSecurityResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"请求被拒绝\"}");
    }

    /**
     * 发送限流响应（429 Too Many Requests）
     */
    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\"}");
    }

    /**
     * 发送 Method 不允许响应（405 Method Not Allowed）
     */
    private void sendMethodNotAllowedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":405,\"message\":\"请求方法不允许\"}");
    }

    /**
     * 发送 IP 已封禁响应（403 Forbidden）
     */
    private void sendBlockedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\"}");
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

    // ===== 公共方法：供外部调用（管理接口使用） =====

    /**
     * 获取当前封禁的 IP 列表
     */
    public Set<String> getBlockedIps() {
        return blockedIps;
    }

    /**
     * 手动封禁 IP
     */
    public void manualBlockIp(String ip) {
        blockedIps.add(ip);
        logger.warn("手动封禁 IP: {}", ip);
    }

    /**
     * 解封 IP
     */
    public void unblockIp(String ip) {
        blockedIps.remove(ip);
        ipBlockCounts.remove(ip);
        logger.info("解封 IP: {}", ip);
    }

    /**
     * 清空黑名单
     */
    public void clearBlacklist() {
        int count = blockedIps.size();
        blockedIps.clear();
        ipBlockCounts.clear();
        logger.info("清空 IP 黑名单，共解封 {} 个 IP", count);
    }

    /**
     * 获取 IP 拦截次数统计
     */
    public Map<String, Integer> getBlockCounts() {
        Map<String, Integer> counts = new HashMap<>();
        ipBlockCounts.forEach((ip, count) -> counts.put(ip, count.get()));
        return counts;
    }

    /**
     * 获取IP请求计数缓存条目（用于缓存管理）
     */
    public Map<String, Object> getIpRequestCountEntries() {
        Map<String, Object> entries = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        ipRequestCounts.forEach((ip, count) -> {
            Map<String, Object> ipData = new HashMap<>();
            ipData.put("requestCount", count.get());
            Long resetTime = ipRequestResetTime.get(ip);
            if (resetTime != null) {
                ipData.put("resetTime", resetTime);
                ipData.put("remainingTime", Math.max(0, resetTime - currentTime) + "ms");
            }
            entries.put(ip, ipData);
        });

        return entries;
    }

    /**
     * 获取IP封禁计数缓存条目（用于缓存管理）
     */
    public Map<String, Object> getIpBlockCountEntries() {
        Map<String, Object> entries = new HashMap<>();

        ipBlockCounts.forEach((ip, count) -> {
            Map<String, Object> ipData = new HashMap<>();
            ipData.put("blockCount", count.get());
            ipData.put("isBlocked", blockedIps.contains(ip));
            ipData.put("threshold", blockThreshold);
            entries.put(ip, ipData);
        });

        return entries;
    }

    /**
     * 获取封禁IP缓存条目（用于缓存管理）
     */
    public Map<String, Object> getBlockedIpEntries() {
        Map<String, Object> entries = new HashMap<>();

        blockedIps.forEach(ip -> {
            Map<String, Object> ipData = new HashMap<>();
            ipData.put("blocked", true);
            AtomicInteger blockCount = ipBlockCounts.get(ip);
            if (blockCount != null) {
                ipData.put("totalBlockCount", blockCount.get());
            }
            entries.put(ip, ipData);
        });

        return entries;
    }
}
