package sg.nus.edu.shopping_cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.nus.edu.shopping_cart.model.ProductViewLog;
import sg.nus.edu.shopping_cart.repository.ProductViewLogRepository;

@Service
public class ProductViewLogService {

    @Autowired
    private ProductViewLogRepository repository;

    @Transactional
    public ProductViewLog save(ProductViewLog log) {
        return repository.save(log);
    }

    @Transactional (readOnly = false)
    public List<ProductViewLog> findByUsername(String username) {
        return repository.findByUsernameOrderByCreatedAtDesc(username);
    }
}

