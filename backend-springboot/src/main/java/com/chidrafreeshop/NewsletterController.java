package com.chidrafreeshop;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "*")
public class NewsletterController {
    private final NewsletterSubscriberRepository subscriberRepository;

    public NewsletterController(NewsletterSubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Adresse email invalide."));
        }
        if (subscriberRepository.findByEmailIgnoreCase(email).isEmpty()) {
            subscriberRepository.save(new NewsletterSubscriber(email));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Inscription confirmée."));
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String email) {
        subscriberRepository.deleteByEmailIgnoreCase(email.trim());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/subscribers")
    public Iterable<NewsletterSubscriber> adminSubscribers() {
        return subscriberRepository.findAllByOrderByIdDesc();
    }

    @DeleteMapping("/admin/subscribers")
    public ResponseEntity<Void> removeSubscriber(@RequestParam String email) {
        subscriberRepository.deleteByEmailIgnoreCase(email.trim());
        return ResponseEntity.noContent().build();
    }

}