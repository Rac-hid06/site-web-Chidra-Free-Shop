package com.chidrafreeshop;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserAccountRepository users;

    @Mock
    private AuthSessionRepository sessions;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(users, sessions);
    }

    @Test
    void registrationStoresOnlyBcryptHashAndCreatesOpaqueSession() {
        when(users.existsByEmailIgnoreCase("customer@example.com")).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.save(any(AuthSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthResult result = authService.register("Customer", " Customer@Example.com ", "a-secure-password");

        assertEquals("customer", result.user().getRole());
        assertEquals("customer@example.com", result.user().getEmail());
        assertNotEquals("a-secure-password", result.user().getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("a-secure-password", result.user().getPasswordHash()));
        assertEquals(43, result.token().length());

        ArgumentCaptor<AuthSession> session = ArgumentCaptor.forClass(AuthSession.class);
        verify(sessions).save(session.capture());
        assertNotEquals(result.token(), session.getValue().getTokenHash());
        assertEquals(AuthService.hashToken(result.token()), session.getValue().getTokenHash());
        assertTrue(session.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("customer@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> authService.register("Customer", "customer@example.com", "a-secure-password")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void registrationRequiresTenCharacterPassword() {
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> authService.register("Customer", "customer@example.com", "short")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        UserAccount user = new UserAccount(
            "Customer",
            "customer@example.com",
            new BCryptPasswordEncoder().encode("correct-password"),
            "customer"
        );
        when(users.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> authService.login("customer@example.com", "incorrect-password")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginIssuesSessionForValidPassword() {
        UserAccount user = new UserAccount(
            "Customer",
            "customer@example.com",
            new BCryptPasswordEncoder().encode("correct-password"),
            "customer"
        );
        when(users.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(sessions.save(any(AuthSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthService.AuthResult result = authService.login("CUSTOMER@example.com", "correct-password");

        assertNotNull(result.token());
        assertEquals(user, result.user());
        verify(sessions).deleteByExpiresAtBefore(any(Instant.class));
    }

    @Test
    void tokenHashIsDeterministicAndDoesNotExposeInput() {
        String token = "opaque-session-token";

        String hash = AuthService.hashToken(token);

        assertNotEquals(token, hash);
        assertEquals(64, hash.length());
        assertFalse(hash.contains(token));
        assertEquals(hash, AuthService.hashToken(token));
    }
}
