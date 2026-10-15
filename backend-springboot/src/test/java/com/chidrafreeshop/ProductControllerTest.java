package com.chidrafreeshop;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(productRepository);
    }

    @Test
    void getProductsReturnsRepositoryProducts() {
        List<Product> products = List.of(new Product());
        when(productRepository.findAll()).thenReturn(products);

        assertSame(products, controller.getProducts());
        verify(productRepository).findAll();
    }

    @Test
    void getAdminProductsReturnsRepositoryProducts() {
        List<Product> products = List.of(new Product());
        when(productRepository.findAll()).thenReturn(products);

        assertSame(products, controller.getAdminProducts());
    }

    @Test
    void addProductSavesAndReturnsCreatedProduct() {
        Product product = new Product("Tee", "T-shirts", 9500, null, "image", "NEW", "new");
        when(productRepository.save(product)).thenReturn(product);

        ResponseEntity<Product> response = controller.addProduct(product);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(product, response.getBody());
        verify(productRepository).save(product);
    }

    @Test
    void getProductByIdReturnsProductWhenFound() {
        Product product = new Product();
        when(productRepository.findById(4L)).thenReturn(Optional.of(product));

        assertSame(product, controller.getProductById(4L));
    }

    @Test
    void getProductByIdThrowsNotFoundWhenMissing() {
        when(productRepository.findById(4L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> controller.getProductById(4L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateProductCopiesAllEditableFieldsAndSavesExistingProduct() {
        Product existing = new Product();
        Product update = new Product("Updated", "Polo", 12000, 15000, "new-image", "SALE", "sale");
        when(productRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        assertSame(existing, controller.updateProduct(4L, update));

        assertEquals("Updated", existing.getName());
        assertEquals("Polo", existing.getCat());
        assertEquals(12000, existing.getPrice());
        assertEquals(15000, existing.getOldPrice());
        assertEquals("new-image", existing.getImage());
        assertEquals("SALE", existing.getBadge());
        assertEquals("sale", existing.getBadgeType());
        verify(productRepository).save(existing);
    }

    @Test
    void updateProductThrowsNotFoundWhenMissing() {
        when(productRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
            () -> controller.updateProduct(4L, new Product()));
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteProductDeletesExistingProduct() {
        when(productRepository.existsById(4L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteProduct(4L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productRepository).deleteById(4L);
    }

    @Test
    void deleteProductThrowsNotFoundWhenMissing() {
        when(productRepository.existsById(4L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> controller.deleteProduct(4L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(productRepository, never()).deleteById(4L);
    }

    @Test
    void checkoutReturnsSuccessfulOrangeMoneyResponse() {
        ResponseEntity<Map<String, Object>> response = controller.checkout(Map.of("total", 9500));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("https://www.orange.com/", response.getBody().get("paymentUrl"));
    }
}