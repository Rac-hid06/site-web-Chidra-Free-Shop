package com.chidrafreeshop;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductRepository productRepository;
    private final BrevoEmailService brevoEmailService;

    @Autowired
    public ProductController(ProductRepository productRepository, BrevoEmailService brevoEmailService) {
        this.productRepository = productRepository;
        this.brevoEmailService = brevoEmailService;
    }

    public ProductController(ProductRepository productRepository) {
        this(productRepository, null);
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/admin/products")
    public List<Product> getAdminProducts() {
        return productRepository.findAll();
    }

    @PostMapping("/admin/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        if (brevoEmailService != null) brevoEmailService.notifyNewProduct(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));
    }

    @PutMapping("/admin/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé"));
        existing.setName(product.getName());
        existing.setCat(product.getCat());
        existing.setPrice(product.getPrice());
        existing.setOldPrice(product.getOldPrice());
        existing.setImage(product.getImage());
        existing.setBadge(product.getBadge());
        existing.setBadgeType(product.getBadgeType());
        return productRepository.save(existing);
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé");
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout/orangemoney")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, Object> order) {
        // Ici on simule un appel Orange Money. En production, remplace par une intégration réelle.
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Commande reçue. Suis les instructions Orange Money.",
            "paymentUrl", "https://www.orange.com/"
        ));
    }
}
