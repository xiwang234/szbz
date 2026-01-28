package xw.szbz.cn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import xw.szbz.cn.interceptor.SignatureInterceptor;

/**
 * Web MVC 配置类
 * 注册拦截器、CORS跨域配置等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SignatureInterceptor signatureInterceptor;

    public WebMvcConfig(SignatureInterceptor signatureInterceptor) {
        this.signatureInterceptor = signatureInterceptor;
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册签名验证拦截器
        registry.addInterceptor(signatureInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api/** 路径
                .excludePathPatterns(
                        "/api/web-auth/register", // 排除注册接口
                        "/api/bazi/**"            // 排除八字接口
                );
    }

    /**
     * 配置 CORS 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源（域名/IP + 端口）
                // 开发环境：允许所有源
                // 生产环境：建议配置具体的前端域名
                .allowedOriginPatterns("*")

                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")

                // 允许的请求头
                .allowedHeaders("*")

                // 是否允许发送 Cookie
                .allowCredentials(true)

                // 暴露的响应头（前端可以访问的响应头）
                .exposedHeaders(
                    "Authorization",
                    "Content-Type",
                    "X-Timestamp",
                    "X-Nonce",
                    "X-Signature"
                )

                // 预检请求的有效期（秒）
                // 在此期间，不需要再发送预检请求
                .maxAge(3600);
    }
}
