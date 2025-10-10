package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.nus.edu.shopping_cart.model.Order;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // return latest active order, identified by username
    @Query("SELECT o FROM Order o WHERE o.customer.username = :username AND o.orderStatus = 'ACTIVE'")
    public Optional<Order> findActiveOrderByUsername(@Param("username") String username);
}
