package com.chidrafreeshop;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BrevoEmailService {
    private final RestClient restClient;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public BrevoEmailService(
        NewsletterSubscriberRepository subscriberRepository,
        @Value("${brevo.api-key:}") String apiKey,
        @Value("${brevo.sender-email:}") String senderEmail,
        @Value("${brevo.sender-name:Chidra Free Shop}") String senderName) {
        this.restClient = RestClient.builder().baseUrl("https://api.brevo.com/v3").build();
        this.subscriberRepository = subscriberRepository;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    public int notifyNewProduct(Product product) {
        if (apiKey.isBlank() || senderEmail.isBlank()) return 0;
        List<NewsletterSubscriber> subscribers = subscriberRepository.findAll();
        int sent = 0;
        for (NewsletterSubscriber subscriber : subscribers) {
            try {
                restClient.post().uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                        "sender", Map.of("name", senderName, "email", senderEmail),
                        "to", List.of(Map.of("email", subscriber.getEmail())),
                        "subject", "Nouveau produit chez Chidra Free Shop",
                        "htmlContent", "<h1>Nouveau produit</h1><p>" + escape(product.getName()) + " est maintenant disponible.</p>"
                    ))
                    .retrieve().toBodilessEntity();
                sent++;
            } catch (RuntimeException ignored) {
                // A mail failure must not cancel a successful product creation.
            }
        }
        return sent;
    }

    private String escape(String value) {
        return value == null ? "Produit" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}