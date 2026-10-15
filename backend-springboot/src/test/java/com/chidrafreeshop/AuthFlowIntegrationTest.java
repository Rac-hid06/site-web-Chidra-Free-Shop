package com.chidrafreeshop;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:auth-flow;DB_CLOSE_DELAY=-1",
    "admin.email=integration-admin@example.com",
    "admin.password=integration-admin-password"
})
@AutoConfigureMockMvc
@Transactional
class AuthFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void customerRegistrationLoginLogoutAndAdminAuthorizationWorkEndToEnd() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test Customer","email":"customer@example.com","password":"customer-password"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.role").value("customer"))
            .andExpect(jsonPath("$.token").doesNotExist())
            .andReturn();

        String customerToken = tokenFrom(registration);
        String sessionCookie = registration.getResponse().getHeader("Set-Cookie");
        org.junit.jupiter.api.Assertions.assertTrue(sessionCookie.contains("HttpOnly"));
        org.junit.jupiter.api.Assertions.assertTrue(sessionCookie.contains("Secure"));
        org.junit.jupiter.api.Assertions.assertTrue(sessionCookie.contains("SameSite=None"));
        mockMvc.perform(get("/api/auth/me").with(cookie(customerToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("customer@example.com"));
        mockMvc.perform(get("/api/admin/products").with(cookie(customerToken)))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout").with(cookie(customerToken)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/me").with(cookie(customerToken)))
            .andExpect(status().isUnauthorized());

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"integration-admin@example.com","password":"integration-admin-password"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("admin"))
            .andReturn();

        mockMvc.perform(get("/api/admin/products").with(cookie(tokenFrom(adminLogin))))
            .andExpect(status().isOk());
    }

    @Test
    void registrationRejectsShortPasswordsAndDuplicateEmails() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test Customer","email":"short@example.com","password":"short"}
                    """))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test Customer","email":"duplicate@example.com","password":"customer-password"}
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Test Customer","email":"duplicate@example.com","password":"customer-password"}
                    """))
            .andExpect(status().isConflict());
    }

    private String tokenFrom(MvcResult result) throws Exception {
        jakarta.servlet.http.Cookie cookie = result.getResponse().getCookie(ApiAuthenticationFilter.SESSION_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            throw new AssertionError("Authentication response did not set the session cookie.");
        }
        return cookie.getValue();
    }

    private static RequestPostProcessor cookie(String token) {
        return request -> {
            request.setCookies(new jakarta.servlet.http.Cookie(ApiAuthenticationFilter.SESSION_COOKIE, token));
            return request;
        };
    }
}
