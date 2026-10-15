package com.chidrafreeshop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String SESSION_COOKIE = "CHIDRA_SESSION";
    public static final String AUTHENTICATED_USER = "authenticatedUser";
    public static final String AUTHENTICATED_TOKEN_HASH = "authenticatedTokenHash";

    private final AuthSessionRepository sessions;
    private final java.util.Set<String> allowedOrigins;

    public ApiAuthenticationFilter(
        AuthSessionRepository sessions,
        @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:https://site-web-chidra-free-shop.vercel.app}") String allowedOrigins
    ) {
        this.sessions = sessions;
        this.allowedOrigins = java.util.Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requestPath(request).startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (!allowCors(request, response)) {
            return;
        }
        if (!isProtectedRoute(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = sessionToken(request);
        if (token == null) {
            unauthorized(response);
            return;
        }
        String tokenHash = AuthService.hashToken(token);
        AuthSession session = sessions.findByTokenHashAndExpiresAtAfter(tokenHash, Instant.now()).orElse(null);
        if (session == null) {
            unauthorized(response);
            return;
        }

        String path = requestPath(request);
        boolean adminRoute = path.startsWith("/api/admin/") || path.startsWith("/api/newsletter/admin/");
        boolean canManageNewsletter = "DELETE".equals(request.getMethod())
            && "/api/newsletter/subscribe".equals(path);
        if ((adminRoute || canManageNewsletter) && !"admin".equals(session.getUser().getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès réservé à l’administration.");
            return;
        }

        request.setAttribute(AUTHENTICATED_USER, session.getUser());
        request.setAttribute(AUTHENTICATED_TOKEN_HASH, tokenHash);
        filterChain.doFilter(request, response);
    }

    private boolean allowCors(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String origin = request.getHeader("Origin");
        if (origin != null && !allowedOrigins.contains(origin)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin is not allowed.");
            return false;
        }
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Vary", "Origin");
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return false;
        }
        return true;
    }

    private static String sessionToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (SESSION_COOKIE.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Session absente ou expirée. Connecte-toi à nouveau.\"}");
    }

    private static String requestPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI().substring(request.getContextPath().length());
        }
        return path;
    }

    private static boolean isProtectedRoute(HttpServletRequest request) {
        String path = requestPath(request);
        return path.equals("/api/auth/me")
            || path.equals("/api/auth/logout")
            || path.startsWith("/api/admin/")
            || path.startsWith("/api/newsletter/admin/")
            || ("DELETE".equals(request.getMethod()) && "/api/newsletter/subscribe".equals(path));
    }
}
