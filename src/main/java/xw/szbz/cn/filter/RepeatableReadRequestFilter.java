package xw.szbz.cn.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求包装过滤器
 * 将 HttpServletRequest 包装为可重复读取的请求
 */
@Component
public class RepeatableReadRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String contentType = httpRequest.getContentType();

            // 只对包含 Body 的请求进行包装
            if (contentType != null && contentType.contains("application/json")) {
                RepeatableReadHttpServletRequest repeatableRequest =
                        new RepeatableReadHttpServletRequest(httpRequest);
                chain.doFilter(repeatableRequest, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
