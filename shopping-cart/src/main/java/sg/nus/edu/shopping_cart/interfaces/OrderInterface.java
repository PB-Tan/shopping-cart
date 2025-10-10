package sg.nus.edu.shopping_cart.interfaces;

import java.util.List;
import java.util.Optional;

import sg.nus.edu.shopping_cart.model.Order;
import sg.nus.edu.shopping_cart.model.OrderItem;

public interface OrderInterface {

    public Order findOrderById(int id);

    public Optional<Order> findOrderByUsername(String username);

    public void updateShippingMethodForOrder(String username, String shippingMethod);

    public Order createOrderFromCart(String username);

    public List<OrderItem> findOrderItemByUsername(String username);

}
