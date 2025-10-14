package sg.nus.edu.shopping_cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.nus.edu.shopping_cart.model.*;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    // Return cart from customer username

    Optional<Cart> findCartByCustomerUsername(String username);
}
