package com.chidrafreeshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void constructorAndAccessorsPreserveProductData() {
        Product product = new Product("Tee", "T-shirts", 9500, 12000, "image", "NEW", "new");

        assertEquals("Tee", product.getName());
        assertEquals("T-shirts", product.getCat());
        assertEquals(9500, product.getPrice());
        assertEquals(12000, product.getOldPrice());
        assertEquals("image", product.getImage());
        assertEquals("NEW", product.getBadge());
        assertEquals("new", product.getBadgeType());

        product.setId(7L);
        product.setName("Updated Tee");
        product.setCat("Polo");
        product.setPrice(10000);
        product.setOldPrice(null);
        product.setImage("updated-image");
        product.setBadge("SALE");
        product.setBadgeType("sale");

        assertEquals(7L, product.getId());
        assertEquals("Updated Tee", product.getName());
        assertEquals("Polo", product.getCat());
        assertEquals(10000, product.getPrice());
        assertEquals(null, product.getOldPrice());
        assertEquals("updated-image", product.getImage());
        assertEquals("SALE", product.getBadge());
        assertEquals("sale", product.getBadgeType());
    }

    @Test
    void defaultConstructorCreatesEmptyProduct() {
        Product product = new Product();

        assertEquals(null, product.getId());
        assertEquals(null, product.getName());
        assertEquals(null, product.getPrice());
    }
}