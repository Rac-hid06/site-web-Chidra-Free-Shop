package com.chidrafreeshop;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountBootstrap implements CommandLineRunner {
    private final UserAccountRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String email;
    private final String password;

    public AdminAccountBootstrap(
        UserAccountRepository users,
        @Value("${admin.email:}") String email,
        @Value("${admin.password:}") String password
    ) {
        this.users = users;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
            || password.length() < 10 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Set a valid ADMIN_EMAIL and an ADMIN_PASSWORD between 10 and 72 UTF-8 bytes.");
        }
        users.findByEmailIgnoreCase(email).ifPresentOrElse(
            account -> {
                if (!"admin".equals(account.getRole())) {
                    throw new IllegalStateException("ADMIN_EMAIL already belongs to a customer account.");
                }
            },
            () -> users.save(new UserAccount(
                "Administrateur",
                email,
                passwordEncoder.encode(password),
                "admin"
            ))
        );
    }
}
