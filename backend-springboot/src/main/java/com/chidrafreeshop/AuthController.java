package com.chidrafreeshop;

import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final int SESSION_MAX_AGE_SECONDS = 12 * 60 * 60;
    private final AuthService authService;
    private final boolean secureCookie;
    private final String sameSite;

    public AuthController(
        AuthService authService,
        @Value("${auth.cookie-secure:false}") boolean secureCookie,
        @Value("${auth.cookie-same-site:Lax}") String sameSite
    ) {
        this.authService = authService;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @RequestBody RegisterRequest request,
        HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.register(request.name(), request.email(), request.password());
        addSessionCookie(response, result.token(), SESSION_MAX_AGE_SECONDS);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(result.user()));
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());
        addSessionCookie(response, result.token(), SESSION_MAX_AGE_SECONDS);
        return toUserResponse(result.user());
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        UserAccount user = (UserAccount) request.getAttribute(ApiAuthenticationFilter.AUTHENTICATED_USER);
        if (user == null) {
            user = authService.findAuthenticatedUser(tokenHash(request));
        }
        return toUserResponse(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        String tokenHash = (String) request.getAttribute(ApiAuthenticationFilter.AUTHENTICATED_TOKEN_HASH);
        if (tokenHash == null) {
            tokenHash = tokenHash(request);
        }
        authService.logout(tokenHash);
        addSessionCookie(response, "", 0);
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie."));
    }

    private static String tokenHash(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ApiAuthenticationFilter.SESSION_COOKIE.equals(cookie.getName())) {
                    return AuthService.hashToken(cookie.getValue());
                }
            }
        }
        throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Session absente ou expirée. Connecte-toi à nouveau."
        );
    }

    private void addSessionCookie(HttpServletResponse response, String token, int maxAge) {
        String cookie = ApiAuthenticationFilter.SESSION_COOKIE + "=" + token
            + "; Path=/api; HttpOnly; Max-Age=" + maxAge
            + "; SameSite=" + sameSite
            + (secureCookie ? "; Secure" : "");
        response.addHeader("Set-Cookie", cookie);
    }

    private static UserResponse toUserResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public record RegisterRequest(String name, String email, String password) {}
    public record LoginRequest(String email, String password) {}
    public record UserResponse(Long id, String name, String email, String role) {}
}
