package sg.nus.edu.shopping_cart.interfaces;

import java.util.*;

import sg.nus.edu.shopping_cart.model.Order;
import sg.nus.edu.shopping_cart.model.OrderItem;

public interface OrderInterface {

    public Order findOrderById(int id);

    public Optional<Order> findTopOrderByUsername(String username);

    public List<Order> findAllOrdersByUsername(String username);

    public void updateShippingMethodForOrder(String username, String shippingMethod);

    public Order createOrderFromCart(String username);

    public List<OrderItem> findOrderItemByUsername(String username);

    public List<OrderItem> findOrderItemByOrderId(int id);

}
