package sg.nus.edu.shopping_cart.service;

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
}

