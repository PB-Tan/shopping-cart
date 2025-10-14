package sg.nus.edu.shopping_cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.nus.edu.shopping_cart.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    public List<OrderItem> findAllByOrderId(int orderId);

    // Check if user has purchased a specific product
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi " +
           "WHERE oi.product.id = :productId " +
           "AND oi.order.customer.username = :username")
    boolean hasUserPurchasedProduct(@Param("productId") int productId, @Param("username") String username);
}
