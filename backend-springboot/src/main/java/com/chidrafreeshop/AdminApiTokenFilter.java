package com.chidrafreeshop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminApiTokenFilter extends OncePerRequestFilter {
    private final byte[] configuredToken;

    public AdminApiTokenFilter(@Value("${admin.api-token:}") String configuredToken) {
        this.configuredToken = configuredToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        boolean adminRoute = path.startsWith("/api/admin/")
            || path.startsWith("/api/newsletter/admin/");
        boolean newsletterUnsubscribe = "DELETE".equals(request.getMethod())
            && "/api/newsletter/subscribe".equals(path);
        return !adminRoute && !newsletterUnsubscribe;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String bearerToken = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring("Bearer ".length())
            : "";

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        if (configuredToken.length == 0) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Admin API token is not configured");
            return;
        }
        if (!MessageDigest.isEqual(
            configuredToken,
            bearerToken.getBytes(StandardCharsets.UTF_8)
        )) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid admin API token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
