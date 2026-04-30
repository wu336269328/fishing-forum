package com.fishforum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Map<String, Deque<Long>> REQUESTS = new ConcurrentHashMap<>();

    private final Set<String> trustedProxies;

    public RateLimitFilter(@Value("${app.security.trusted-proxies:}") String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(proxy -> !proxy.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Limit limit = limitFor(request);
        if (limit != null && exceeded(request, limit)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Limit limitFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && (path.equals("/api/auth/login") || path.equals("/api/auth/register"))) {
            return new Limit("auth", 12);
        }
        if ("POST".equals(method) && path.startsWith("/api/upload")) {
            return new Limit("upload", 20);
        }
        if ("POST".equals(method) && path.equals("/api/comments")) {
            return new Limit("comment", 30);
        }
        if ("POST".equals(method) && path.equals("/api/reports")) {
            return new Limit("report", 10);
        }
        return null;
    }

    private boolean exceeded(HttpServletRequest request, Limit limit) {
        String key = limit.scope + ":" + clientIp(request);
        long now = System.currentTimeMillis();
        long min = now - WINDOW.toMillis();
        Deque<Long> timestamps = REQUESTS.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < min) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= limit.maxRequests) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (trustedProxies.contains(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }

    private record Limit(String scope, int maxRequests) {
    }
}
