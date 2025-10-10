package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.nus.edu.shopping_cart.model.*;

public interface CartRepository extends JpaRepository<Cart, Integer> {

}
