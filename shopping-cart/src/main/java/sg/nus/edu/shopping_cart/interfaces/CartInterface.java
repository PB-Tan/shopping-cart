package sg.nus.edu.shopping_cart.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import sg.nus.edu.shopping_cart.model.*;

public interface CartInterface {

    public List<Product> findAllProduct();

    public Optional<Product> findProduct(int productId);

    public Cart getCartByCustomer(String username);

    public List<CartItem> getCartItemsByCustomer(String username);

    public Cart addProductToCart(String username, int productId, int quantity);

    public Cart updateCartItem(String username, int productId, int quantity);

    public Cart deleteCartItem(String username, int productId);

    public BigDecimal calculateCartTotal(String username);

    public void clearCart(String username);

}
