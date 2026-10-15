package com.chidrafreeshop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);
    List<NewsletterSubscriber> findAllByOrderByIdDesc();
    void deleteByEmailIgnoreCase(String email);
}