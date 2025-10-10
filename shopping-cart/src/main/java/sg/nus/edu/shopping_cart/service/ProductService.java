package sg.nus.edu.shopping_cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.repository.*;
import sg.nus.edu.shopping_cart.model.Product;

@Service
@Transactional(readOnly = true)
public class ProductService implements ProductInterface {

    @Autowired
    public ProductRepository pdtRepo;

    @Override
    public Product save(Product product) {
        // TODO Auto-generated method stub
        return pdtRepo.save(product);
    }

    @Override
    @Transactional
    public List<Product> findProductByNameContainingIgnoreCase(String name) {
        // TODO Auto-generated method stub
        return pdtRepo.findProductByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional
    public List<Product> findProductByCategoryContainingIgnoreCase(String category) {
        // TODO Auto-generated method stub
        return pdtRepo.findProductByCategoryContainingIgnoreCase(category);
    }

    @Override
    @Transactional
    public List<Product> findAll() {
        // TODO Auto-generated method stub
        return pdtRepo.findAll();
    }
}
