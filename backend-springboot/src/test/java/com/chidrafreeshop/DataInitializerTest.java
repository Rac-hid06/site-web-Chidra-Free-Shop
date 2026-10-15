package com.chidrafreeshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void runSeedsEightProductsWhenRepositoryIsEmpty() {
        when(productRepository.count()).thenReturn(0L);

        new DataInitializer(productRepository).run();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(8)).save(productCaptor.capture());
        assertEquals(8, productCaptor.getAllValues().size());
        assertEquals("Tee Oversize Urban", productCaptor.getAllValues().get(0).getName());
        assertEquals("Jean Slim Urbain", productCaptor.getAllValues().get(7).getName());
    }

    @Test
    void runDoesNotSeedProductsWhenRepositoryIsNotEmpty() {
        when(productRepository.count()).thenReturn(1L);

        new DataInitializer(productRepository).run("ignored");

        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any(Product.class));
    }
}