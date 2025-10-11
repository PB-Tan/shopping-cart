package sg.nus.edu.shopping_cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.repository.*;

import java.util.*;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class OrderService implements OrderInterface {

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    ShipmentRepository shipRepo;

    @Autowired
    CustomerRepository customerRepo;

    @Autowired
    CartItemRepository cartItemRepo;

    @Autowired
    OrderItemRepository orderItemRepo;

    @Override
    public Order findOrderById(int id) {
        return orderRepo.findById(id).get();
    }

    @Override
    public Optional<Order> findTopOrderByUsername(String username) {
        return orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username, "ACTIVE");
    }

    @Override
    public List<Order> findAllOrdersByUsername(String username) {
        return orderRepo.findAllByCustomerUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateShippingMethodForOrder(String username, String shippingMethod) {
        Optional<Order> activeOrder = orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username,
                "ACTIVE");
        if (activeOrder.isPresent()) {
            Order order = activeOrder.get();
            Shipment shipment = order.getShipment();

            // if no shipment has been found prior, then persist into
            // a new shipment
            // assoicated with the order
            if (shipment == null) {
                shipment = new Shipment();
                shipRepo.save(shipment); // assigns id with
            }

            // update shipment method for new shipment
            shipment.setShipmentMethod(shippingMethod);
            order.setShipment(shipment);
            orderRepo.save(order);
        }

        // if no active orders found, then do nothing
    }

    @Override
    @Transactional(readOnly = false)
    public Order createOrderFromCart(String username) {
        // If an ACTIVE order already exists, reuse it
        Optional<Order> existingActive = orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username,
                "ACTIVE");
        if (existingActive.isPresent()) {
            return existingActive.get();
        }

        // If customer checks out an empty cart, return an empty order
        List<CartItem> cartItems = cartItemRepo.findAllCartItemsByCustomer(username);
        if (cartItems == null || cartItems.isEmpty())
            return new Order();

        // Creating a new order and setting prelim attributes
        Order order = new Order();
        order.setCustomer(customerRepo.findById(username).get());
        order.setStatus("ACTIVE");
        order.setCreatedAt(LocalDateTime.now());

        double subTotal = 0.0;
        for (CartItem cartItem : cartItems) {
            subTotal += cartItem.getUnitPrice() * cartItem.getQuantity();
        }
        order.setGrandTotal(subTotal);
        order = orderRepo.save(order);

        // Building order Items with cartItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setItemTotal(orderItem.getUnitPrice() * orderItem.getQuantity());

            // persist new orderItem into entity and add to list of OrderItems
            orderItemRepo.save(orderItem);
            orderItems.add(orderItem);
        }
        // persist order associated with list of newly created orderItems
        order.setOrderItems(orderItems);
        orderRepo.save(order);

        // clear cart after purchase is made
        cartItemRepo.deleteAll(cartItems);

        return order;
    }

    // lambda expression for finding order items in the active order. --> display
    // inside cart UI view
    @Override
    public List<OrderItem> findOrderItemByUsername(String username) {
        return findTopOrderByUsername(username)
                .map(Order::getOrderItems)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<OrderItem> findOrderItemByOrderId(int id) {
        return orderItemRepo.findAllByOrderId(id);
    }

}
