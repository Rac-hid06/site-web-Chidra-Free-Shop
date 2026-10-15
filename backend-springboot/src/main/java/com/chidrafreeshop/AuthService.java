package com.chidrafreeshop;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final int TOKEN_BYTES = 32;
    private static final int SESSION_HOURS = 12;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserAccountRepository users, AuthSessionRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @Transactional
    public AuthResult register(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        validateName(name);
        validatePassword(password);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse email est déjà utilisée.");
        }
        UserAccount user = users.save(new UserAccount(
            name.trim(),
            normalizedEmail,
            passwordEncoder.encode(password),
            "customer"
        ));
        return createSession(user);
    }

    @Transactional
    public AuthResult login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email et mot de passe requis.");
        }
        UserAccount user = users.findByEmailIgnoreCase(normalizedEmail)
            .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect."));
        return createSession(user);
    }

    @Transactional
    public void logout(String tokenHash) {
        sessions.deleteById(tokenHash);
    }

    public UserAccount findAuthenticatedUser(String tokenHash) {
        return sessions.findByTokenHashAndExpiresAtAfter(tokenHash, Instant.now())
            .map(AuthSession::getUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée."));
    }

    public static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private AuthResult createSession(UserAccount user) {
        sessions.deleteByExpiresAtBefore(Instant.now());
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.save(new AuthSession(hashToken(token), user, Instant.now().plus(SESSION_HOURS, ChronoUnit.HOURS)));
        return new AuthResult(token, user);
    }

    private static String normalizeEmail(String email) {
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse email invalide.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire (120 caractères maximum).");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 10
            || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Le mot de passe doit contenir au moins 10 caractères et au maximum 72 octets UTF-8."
            );
        }
    }

    public record AuthResult(String token, UserAccount user) {}
}
