package sg.nus.edu.shopping_cart;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.repository.*;

@SpringBootTest
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepo;

    @Test
    public void testSaveNewProduct() { 
        Product product = new Product();
        product.setName("Fake Product");
        product.setStock(100);
        product.setUnitPrice(1.11);
        Product savedProduct = productRepo.save(product);

        //verification
        Product verifyProduct = productRepo.findById(savedProduct.getId()).orElse(null);
        assertNotNull(verifyProduct); //ensure it exists
        assertEquals("Fake Product", verifyProduct.getName()); //ensure name matches
        assertTrue(verifyProduct.getUnitPrice() == savedProduct.getUnitPrice()); //ensure price matches
    }

    @Test
    public void findProductByCategoryContainingIgnoreCaseMatchesPeripherals() { 
        List<Product> existedProducts = productRepo.findProductByCategoryContainingIgnoreCase("peripherals");

        //verification
        assertNotNull(existedProducts); //ensure peripherals exist
        assertEquals(2, existedProducts.size()); //ensure number of products = 2

        List<String> names = existedProducts.stream().map(Product::getName).toList();
        assertTrue(names.contains("Wireless Mouse"));
        assertTrue(names.contains("USB-C Hub"));
    }
}