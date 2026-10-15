package com.chidrafreeshop;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class ApiAuthenticationFilterTest {
    @Mock
    private AuthSessionRepository sessions;

    @Test
    void adminRouteRejectsMissingToken() throws Exception {
        ApiAuthenticationFilter filter = filter();
        MockHttpServletRequest request = request("GET", "/api/admin/products");
        request.addHeader("Origin", "https://site-web-chidra-free-shop.vercel.app");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals("https://site-web-chidra-free-shop.vercel.app", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
    }

    @Test
    void adminRouteRejectsCustomerSession() throws Exception {
        String token = "customer-session";
        UserAccount customer = account("customer");
        when(sessions.findByTokenHashAndExpiresAtAfter(eq(AuthService.hashToken(token)), any(Instant.class)))
            .thenReturn(Optional.of(new AuthSession(AuthService.hashToken(token), customer, Instant.now().plusSeconds(60))));
        MockHttpServletRequest request = request("DELETE", "/api/admin/products/1");
        request.setCookies(new jakarta.servlet.http.Cookie(ApiAuthenticationFilter.SESSION_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
    }

    @Test
    void adminRouteAcceptsValidAdminSession() throws Exception {
        String token = "admin-session";
        UserAccount admin = account("admin");
        when(sessions.findByTokenHashAndExpiresAtAfter(eq(AuthService.hashToken(token)), any(Instant.class)))
            .thenReturn(Optional.of(new AuthSession(AuthService.hashToken(token), admin, Instant.now().plusSeconds(60))));
        MockHttpServletRequest request = request("POST", "/api/admin/products");
        request.setCookies(new jakarta.servlet.http.Cookie(ApiAuthenticationFilter.SESSION_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter().doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertSame(admin, request.getAttribute(ApiAuthenticationFilter.AUTHENTICATED_USER));
        verify(sessions).findByTokenHashAndExpiresAtAfter(eq(AuthService.hashToken(token)), any(Instant.class));
    }

    @Test
    void deleteNewsletterSubscriptionRequiresAdminSession() throws Exception {
        String token = "customer-session";
        UserAccount customer = account("customer");
        when(sessions.findByTokenHashAndExpiresAtAfter(eq(AuthService.hashToken(token)), any(Instant.class)))
            .thenReturn(Optional.of(new AuthSession(AuthService.hashToken(token), customer, Instant.now().plusSeconds(60))));
        MockHttpServletRequest request = request("DELETE", "/api/newsletter/subscribe");
        request.setCookies(new jakarta.servlet.http.Cookie(ApiAuthenticationFilter.SESSION_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
    }

    @Test
    void publicProductRouteDoesNotRequireSession() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter().doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
    }

    @Test
    void apiRejectsUnconfiguredBrowserOrigins() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/auth/login");
        request.addHeader("Origin", "https://untrusted.example");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter().doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertEquals(null, response.getHeader("Access-Control-Allow-Origin"));
    }

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private static UserAccount account(String role) {
        return new UserAccount("Account", "account@example.com", "hash", role);
    }

    private ApiAuthenticationFilter filter() {
        return new ApiAuthenticationFilter(sessions, "https://site-web-chidra-free-shop.vercel.app");
    }
}
