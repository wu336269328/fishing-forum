package com.fishforum.config;

import com.fishforum.common.JwtUtil;
import com.fishforum.entity.User;
import com.fishforum.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Spring Security 安全配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    // 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 认证管理器
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 安全过滤链配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 关闭CSRF（前后端分离不需要）
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态会话
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 无需登录
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/uploads/**").permitAll()
                        .requestMatchers("/api/statistics/public").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // 管理员接口（必须在GET公开规则之前！）
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 公开浏览接口：新增公开接口必须显式加入白名单，避免误暴露私有 GET API
                        .requestMatchers(HttpMethod.GET,
                                "/api/posts",
                                "/api/posts/*",
                                "/api/posts/hot",
                                "/api/comments/*",
                                "/api/sections",
                                "/api/tags",
                                "/api/tags/hot",
                                "/api/weather",
                                "/api/spots",
                                "/api/spots/all",
                                "/api/spots/*",
                                "/api/spots/*/reviews",
                                "/api/wiki",
                                "/api/wiki/categories",
                                "/api/wiki/*",
                                "/api/wiki/*/comments",
                                "/api/wiki/*/history",
                                "/api/announcements",
                                "/api/users/*/profile",
                                "/api/users/*/posts",
                                "/api/users/*/followings",
                                "/api/users/*/followers",
                                "/api/follows/check/**",
                                "/api/notifications/unread-count").permitAll()
                        // 上传写接口需要登录，已上传文件访问仍通过 /api/uploads/** 公开
                        .requestMatchers("/api/upload/**").authenticated()
                        // 其余POST/PUT/DELETE需要登录（发帖、评论等写操作）
                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                // 在密码验证过滤器前添加JWT过滤器
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT认证过滤器 - 从请求头提取并验证JWT令牌
     */
    @Component
    @RequiredArgsConstructor
    public static class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final UserMapper userMapper;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    User user = userMapper.selectById(userId);
                    if (user != null && !Boolean.TRUE.equals(user.getIsBanned()) && user.getRole() != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        SecurityContextHolder.clearContext();
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
