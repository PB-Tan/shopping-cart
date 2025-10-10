package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;
import sg.nus.edu.shopping_cart.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    public List<Product> findProductByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.category LIKE %:category%")
    public List<Product> findProductByCategoryContainingIgnoreCase(@Param("category") String category);
}
