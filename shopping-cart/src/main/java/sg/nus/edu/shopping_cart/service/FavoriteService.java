// edit by serene
package sg.nus.edu.shopping_cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.model.Favorite;
import sg.nus.edu.shopping_cart.model.Product;
import sg.nus.edu.shopping_cart.repository.CustomerRepository;
import sg.nus.edu.shopping_cart.repository.FavoriteRepository;
import sg.nus.edu.shopping_cart.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add a product to user's favorites (private - only used by toggleFavorite)
     */
    @Transactional
    private boolean addFavorite(String username, Integer productId) {
        Customer customer = customerRepository.findById(username).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (customer == null || product == null) {
            return false;
        }

        // Check if already favorited
        Optional<Favorite> existing = favoriteRepository.findByCustomerAndProduct(customer, product);
        if (existing.isPresent()) {
            return false; // Already favorited
        }

        Favorite favorite = new Favorite(customer, product);
        favoriteRepository.save(favorite);
        return true;
    }

    /**
     * Remove a product from user's favorites (private - only used by toggleFavorite)
     */
    @Transactional
    private boolean removeFavorite(String username, Integer productId) {
        Customer customer = customerRepository.findById(username).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (customer == null || product == null) {
            return false;
        }

        Optional<Favorite> favorite = favoriteRepository.findByCustomerAndProduct(customer, product);
        if (favorite.isPresent()) {
            favoriteRepository.delete(favorite.get());
            return true;
        }

        return false;
    }

    /**
     * Toggle favorite status
     */
    @Transactional
    public boolean toggleFavorite(String username, Integer productId) {
        if (isFavorited(username, productId)) {
            return removeFavorite(username, productId);
        } else {
            return addFavorite(username, productId);
        }
    }

    /**
     * Check if a product is favorited by user
     */
    public boolean isFavorited(String username, Integer productId) {
        return favoriteRepository.findByCustomerUsernameAndProductId(username, productId).isPresent();
    }

    /**
     * Get all favorite products for a user
     */
    public List<Product> getUserFavoriteProducts(String username) {
        List<Favorite> favorites = favoriteRepository.findByCustomerUsername(username);
        return favorites.stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }

    /**
     * Get all favorites for a user with sorting
     * @param username User's username
     * @param sortBy Sort criteria: "priceAsc", "priceDesc", "timeAsc", "timeDesc"
     * @return Sorted list of favorites
     */
    public List<Favorite> getUserFavoritesSorted(String username, String sortBy) {
        List<Favorite> favorites = favoriteRepository.findByCustomerUsername(username);

        if (sortBy == null || sortBy.isEmpty()) {
            return favorites;
        }

        switch (sortBy) {
            case "priceAsc":
                favorites.sort((f1, f2) ->
                    Double.compare(f1.getProduct().getUnitPrice(), f2.getProduct().getUnitPrice()));
                break;
            case "priceDesc":
                favorites.sort((f1, f2) ->
                    Double.compare(f2.getProduct().getUnitPrice(), f1.getProduct().getUnitPrice()));
                break;
            case "timeAsc":
                favorites.sort((f1, f2) ->
                    f1.getCreatedAt().compareTo(f2.getCreatedAt()));
                break;
            case "timeDesc":
                favorites.sort((f1, f2) ->
                    f2.getCreatedAt().compareTo(f1.getCreatedAt()));
                break;
            default:
                // No sorting
                break;
        }

        return favorites;
    }


    public List<Product> getUserFavoriteProductsSorted(String username, String sortBy) {
        List<Favorite> favorites = getUserFavoritesSorted(username, sortBy);
        return favorites.stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }
}
