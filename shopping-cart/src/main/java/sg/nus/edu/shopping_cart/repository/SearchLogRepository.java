package sg.nus.edu.shopping_cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.nus.edu.shopping_cart.model.SearchLog;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    List<SearchLog> findByUsernameOrderByCreatedAtDesc(String username);
}

