package xw.szbz.cn.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import xw.szbz.cn.util.EnhancedJwtUtil;

/**
 * JWT 认证过滤器
 * 在 Spring Security 过滤器链中验证 JWT Token
 * 如果 Token 有效，将用户信息设置到 SecurityContext 中
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private EnhancedJwtUtil jwtUtil;

    // 不需要 JWT 验证的路径（公开路径）
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/web-auth/register",
        "/api/web-auth/login",
        "/api/web-auth/random-salt",
        "/api/web-auth/request-reset",
        "/api/web-auth/reset-password",
        "/api/web-auth/verify-email",
        "/api/bazi/",  // /api/bazi/** 开头的所有路径
        "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 检查是否是排除的路径
        if (isExcludedPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 从请求头中提取 Token
            String token = extractTokenFromRequest(request);

            // 2. 如果 Token 存在，进行验证
            if (token != null) {
                // 3. 验证 Token 有效性
                if (!jwtUtil.validateToken(token)) {
                    logger.warn("JWT Token 无效, URI: {}", requestUri);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的访问令牌");
                    return;
                }

                // 4. 验证是否为 Access Token
                if (!jwtUtil.isAccessToken(token)) {
                    logger.warn("Token 类型错误（不是 Access Token）, URI: {}", requestUri);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token 类型错误");
                    return;
                }

                // 5. 从 Token 中提取用户信息
                String encryptedUserId = jwtUtil.getEncryptedUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                // 注意：不在这里获取用户详细信息和检查状态
                // 原因：encryptedUserId 是加密的13位字符串，解密需要用户创建时间
                // 用户状态检查由 Controller 层负责

                // 6. 创建 Spring Security 认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    encryptedUserId,  // principal: 加密用户ID（Controller 可以用它来查询用户）
                    null,             // credentials: 不需要密码
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")) // authorities: 用户角色
                );

                // 7. 设置请求详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. 将认证信息设置到 Spring Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("JWT 认证成功, userId: {}, username: {}, URI: {}", encryptedUserId, username, requestUri);
            }

        } catch (Exception e) {
            logger.error("JWT 认证过程发生异常, URI: {}", requestUri, e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "认证失败");
            return;
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取 JWT Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 判断是否是排除的路径（不需要 JWT 验证）
     */
    private boolean isExcludedPath(String requestUri) {
        for (String path : EXCLUDED_PATHS) {
            if (requestUri.equals(path) || requestUri.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null}",
            status, message
        ));
    }
}
