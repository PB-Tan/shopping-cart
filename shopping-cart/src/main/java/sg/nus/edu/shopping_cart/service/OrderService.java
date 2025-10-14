package sg.nus.edu.shopping_cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.repository.*;

import java.util.*;
import java.math.BigDecimal;
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

    @Autowired
    CartRepository cartRepo;

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
        // find top order made by customer
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
        // Check if an ACTIVE order already exists, reuse it
        Optional<Order> existingActive = orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username,
                "ACTIVE");
        if (existingActive.isPresent()) {
            return existingActive.get();
        }

        // If customer cart does not exists, return empty order, controller will prevent
        // checkout
        Optional<Cart> optCart = cartRepo.findCartByCustomerUsername(username);
        if (optCart.isEmpty()) {
            Cart cart = new Cart();
            cart.setCustomer(customerRepo.findById(username).get());
            return new Order();
        }

        // At this point, cart already exists
        Cart cart = optCart.get();

        // If customer checks out an empty cart, return an empty order, controller will
        // prevent checkout
        List<CartItem> cartItems = cartItemRepo.findAllCartItemsByCustomer(username);
        if (cartItems == null || cartItems.isEmpty()) {
            return new Order();
        }

        // Creating a new order and setting prelim attributes from cart
        Order order = new Order();
        order.setCustomer(customerRepo.findById(username).get());
        order.setStatus("ACTIVE");
        order.setDiscountCode(cart.getDiscountCode());
        order.setCreatedAt(LocalDateTime.now());
        order.setGrandTotal(cart.getGrandTotal());
        order = orderRepo.save(order);

        // Building order Items with cartItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setUnitPrice(cartItem.getProduct().getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setItemTotal(cartItem.getProduct().getUnitPrice() * orderItem.getQuantity());
            orderItem.setProductName(cartItem.getProduct().getName());

            // persist new orderItem into entity and add to list of OrderItems
            orderItemRepo.save(orderItem);
            orderItems.add(orderItem);
        }
        // persist order associated with list of newly created orderItems
        order.setOrderItems(orderItems);

        // get cart from customer to get discountTotal attribute
        order.setDiscountCode(cart.getDiscountCode());
        order.setDiscountTotal(cart.getDiscountTotal());

        // finally save the newly created ordeer
        orderRepo.save(order);

        return order;
    }

    @Override
    public void updateStock(Order order) {
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int orderQty = orderItem.getQuantity();
            int currentStock = product.getStock();
            product.setStock(currentStock - orderQty);
        }
    }

    // functional programming for finding order items in the active order. -->
    // display inside cart UI view
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
