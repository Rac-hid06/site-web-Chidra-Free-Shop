package com.chidrafreeshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if(productRepository.count() == 0) {
            productRepository.save(new Product("Tee Oversize Urban", "T-shirts", 9500, null, "https://images.unsplash.com/photo-1520975914428-193b75e88e47?auto=format&fit=crop&w=800&q=80", "NEW", "new"));
            productRepository.save(new Product("Cargo Street Premium", "Pantalons", 18500, 22000, "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=800&q=80", "SOLDE", "sale"));
            productRepository.save(new Product("Cap Chidra Signature", "Casquettes", 6500, null, "https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&w=800&q=80", "", ""));
            productRepository.save(new Product("Hoodie Essentiel Noir", "T-shirts", 14500, null, "https://images.unsplash.com/photo-1521334884684-d80222895322?auto=format&fit=crop&w=800&q=80", "NEW", "new"));
            productRepository.save(new Product("Sneaker Low Runner", "Sneakers", 27000, 32000, "https://images.unsplash.com/photo-1519741497447-200d45c9917c?auto=format&fit=crop&w=800&q=80", "SOLDE", "sale"));
            productRepository.save(new Product("Tote Bag Chidra", "Sacs", 5500, null, "https://images.unsplash.com/photo-1520962919198-7bc58add9ca6?auto=format&fit=crop&w=800&q=80", "", ""));
            productRepository.save(new Product("Polo Élégance BF", "T-shirts", 11000, null, "https://images.unsplash.com/photo-1512436991641-6745cdb1723f?auto=format&fit=crop&w=800&q=80", "NEW", "new"));
            productRepository.save(new Product("Jean Slim Urbain", "Pantalons", 16000, null, "https://images.unsplash.com/photo-1495121605193-b116b5b9c5d6?auto=format&fit=crop&w=800&q=80", "", ""));
        }
    }
}
