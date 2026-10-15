package com.chidrafreeshop;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class NewsletterSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;

    protected NewsletterSubscriber() {}

    public NewsletterSubscriber(String email) {
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}