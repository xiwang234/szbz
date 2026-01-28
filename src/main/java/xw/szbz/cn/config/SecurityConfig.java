package xw.szbz.cn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * 配置哪些接口需要认证，哪些接口可以公开访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（因为使用 JWT，不需要 CSRF 保护）
            .csrf(csrf -> csrf.disable())

            // 配置 Session 管理为无状态（使用 JWT，不使用 Session）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开的接口（不需要认证）
                .requestMatchers(
                    // 注册和登录相关接口
                    "/api/web-auth/register",
                    "/api/web-auth/login",
                    "/api/web-auth/random-salt",
                    "/api/web-auth/request-reset",
                    "/api/web-auth/reset-password",
                    "/api/web-auth/verify-email",

                    // 八字相关接口（公开）
                    "/api/bazi/**",

                    // 错误页面
                    "/error"
                ).permitAll()

                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
