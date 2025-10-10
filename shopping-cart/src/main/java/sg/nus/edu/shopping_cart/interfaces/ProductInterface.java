package sg.nus.edu.shopping_cart.interfaces;

import java.util.List;

import sg.nus.edu.shopping_cart.model.*;

public interface ProductInterface {

    public Product save(Product product);

    public List<Product> findProductByNameContainingIgnoreCase(String name);

    public List<Product> findProductByCategoryContainingIgnoreCase(String category);

    public List<Product> findAll();
}
