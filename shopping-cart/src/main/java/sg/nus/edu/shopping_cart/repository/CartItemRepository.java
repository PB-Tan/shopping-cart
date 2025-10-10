package sg.nus.edu.shopping_cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.nus.edu.shopping_cart.model.CartItem;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    @Query("SELECT ci FROM CartItem ci JOIN ci.cart cart JOIN cart.customer c WHERE c.username = :username")
    public List<CartItem> findAllCartItemsByCustomer(@Param("username") String username);

    @Query("select ci.product.name from CartItem ci where ci.id = :id")
    public String findProductNameById(@Param("id") int id);

    @Query("select ci.unitPrice from CartItem ci where ci.id = :id")
    public double findUnitPriceById(@Param("id") int id);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    public Optional<CartItem> findByCartIdAndProductId(@Param("cartId") int cartId, @Param("productId") int productId);

}
