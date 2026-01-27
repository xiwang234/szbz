package xw.szbz.cn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xw.szbz.cn.interceptor.SignatureInterceptor;

/**
 * Web MVC 配置类
 * 注册拦截器、过滤器等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SignatureInterceptor signatureInterceptor;

    public WebMvcConfig(SignatureInterceptor signatureInterceptor) {
        this.signatureInterceptor = signatureInterceptor;
    }

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
}
