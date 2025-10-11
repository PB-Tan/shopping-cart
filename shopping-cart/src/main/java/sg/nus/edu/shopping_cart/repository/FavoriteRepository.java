// edit by serene
package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.nus.edu.shopping_cart.model.Favorite;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {


    // Find all favorites by username
    List<Favorite> findByCustomerUsername(String username);

    // Check if a favorite exists for a customer and product
    Optional<Favorite> findByCustomerAndProduct(Customer customer, Product product);

    // Check if a favorite exists by username and product id
    Optional<Favorite> findByCustomerUsernameAndProductId(String username, Integer productId);

}
