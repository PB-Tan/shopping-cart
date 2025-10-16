package sg.nus.edu.shopping_cart.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.model.SearchLog;
import sg.nus.edu.shopping_cart.repository.SearchLogRepository;

@Service
public class SearchLogService {

    @Autowired
    private SearchLogRepository repository;

    @Transactional
    public SearchLog save(SearchLog log) {
        return repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<SearchLog> findRecentByUsername(String username) {
        return repository.findByUsernameOrderByCreatedAtDesc(username);
    }
}

