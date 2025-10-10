package sg.nus.edu.shopping_cart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.repository.*;

@Service
@Transactional(readOnly = true)
public class CartService implements CartInterface {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public List<Product> findAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findProduct(int productId) {
        return productRepository.findById(productId);
    }

    // get customer's shoppingcart
    @Override
    public Cart getCartByCustomer(String username) {
        Customer customer = customerRepository.findById(username).get();
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(customer);
            cartRepository.save(cart);// 将绑定了username的购物车放入数据库中
            customer.setCart(cart); // 关联回customer
        }
        return cart;
    }

    // list customer's cart items
    @Override
    public List<CartItem> getCartItemsByCustomer(String username) {
        Customer customer = customerRepository.findById(username).get();
        Cart cart = customer.getCart();
        List<CartItem> items = cart.getCartItems();

        // LJ
        for (CartItem item : items) {
            Product product = item.getProduct();
            int stock = product.getStock(); // 当前库存
            int cartQty = item.getQuantity();

            if (stock < cartQty) {
                item.setStatus("INSUFFICIENT STOCK"); // 售罄
            } else if (stock - cartQty <= 5) {
                item.setStatus("LIMITED STOCK"); // 库存不足（小于或等于5）
            } else {
                item.setStatus("AVAILABLE"); // 正常
            }
        }

        return items;
    }

    // add item'num by quantity
    @Override
    @Transactional(readOnly = false)
    public Cart addProductToCart(String username, int productId, int quantity) {
        Cart cart = getCartByCustomer(username);
        List<CartItem> items = cart.getCartItems();
        if (items == null) {
            items = new ArrayList<>();
            cart.setCartItems(items);
        }
        // assess if product exists
        CartItem existingItem = items.stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null); // if no return null

        Product product = productRepository.findById(productId).get();

        if (existingItem != null) {
            // the num cant > stock
            if (existingItem.getQuantity() + quantity > product.getStock()) {
                existingItem.setStatus("INSUFFICIENT STOCK");
            }
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            // if item does not exist yet
        } else {
            // if stock==0 cant add
            if (product.getStock() <= 0) {
                existingItem.setStatus("INSUFFICIENT STOCK");
                // throw new RuntimeException("Cannot add, no stock available");

            }
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity); // the number of newitem is 1 (default)
            newItem.setCart(cart);
            cart.getCartItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    // change number of item by hand
    @Override
    @Transactional(readOnly = false)
    public Cart updateCartItem(String username, int productId, int quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Cart cart = getCartByCustomer(username);

        // find CartItem（use get()，假设 findFirst() 确保有值）
        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getProduct().getId() == productId)
                .findFirst()
                .get(); // 如果没有，会抛出 NoSuchElementException

        // check quantity
        int stock = item.getProduct().getStock(); // product 的inventory
        if (quantity > stock) {
            item.setStatus("INSUFFICIENT STOCK");
        } else if (quantity > stock + 5) {
            item.setStatus("LIMITED STOCK");
            item.setQuantity(quantity);
        } else {
            // update number
            item.setQuantity(quantity);
        }
        return cartRepository.save(cart);
    }

    // delete item in shoppingcart
    @Override
    @Transactional(readOnly = false)
    public Cart deleteCartItem(String username, int productId) {
        Cart cart = getCartByCustomer(username);

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setCart(null);
            cart.getCartItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            throw new RuntimeException("CartItem not found in cart");
        }

        return cart;
    }

    @Override
    public BigDecimal calculateCartTotal(String username) {
        Cart cart = getCartByCustomer(username);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getCartItems()) {
            double price = item.getProduct().getUnitPrice();
            int quantity = item.getQuantity();
            total = total.add(BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    @Override
    @Transactional(readOnly = false)
    public void clearCart(String username) {

        Cart cart = getCartByCustomer(username);

        // delete each CartItem
        for (CartItem item : cart.getCartItems()) {
            cartItemRepository.delete(item); // delete it from sql
        }

        // delete all
        cart.getCartItems().clear();

        // save change
        cartRepository.save(cart);
    }
}
