package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

import sg.nus.edu.shopping_cart.model.Order;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Return the latest ACTIVE order for a user
    Optional<Order> findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(String username, String status);

    // return all orders for a user
    List<Order> findAllByCustomerUsernameOrderByCreatedAtDesc(String username);
}
