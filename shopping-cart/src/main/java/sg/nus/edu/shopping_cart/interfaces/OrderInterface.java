package sg.nus.edu.shopping_cart.interfaces;

import java.util.Optional;

import sg.nus.edu.shopping_cart.model.Order;

public interface OrderInterface {

    public Order findOrderById(int id);

    public Optional<Order> findActiveOrderByUsername(String username);
}
