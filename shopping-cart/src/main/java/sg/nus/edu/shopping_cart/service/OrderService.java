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
    public Optional<Order> findOrderById(int id) {
        return orderRepo.findById(id);
    }

    @Override
    public Optional<Order> findTopOrderByUsername(String username) {
        return orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username, "PENDING PAYMENT");
    }

    @Override
    public List<Order> findAllOrdersByUsername(String username) {
        return orderRepo.findAllByCustomerUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    @Transactional(readOnly = false)
    public void updateShippingMethodForOrder(String username, String shippingMethod) {
        // find top order made by customer
        Optional<Order> pendingOrder = orderRepo.findTopByCustomerUsernameAndStatusOrderByCreatedAtDesc(username,
                "PENDING PAYMENT");
        if (pendingOrder.isPresent()) {
            Order order = pendingOrder.get();
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
        // assume cart already exists, since controller will prevent execution of this
        // method if otherwise. This method only executes at the checkout
        Cart cart = cartRepo.findCartByCustomerUsername(username).get();
        List<CartItem> cartItems = cartItemRepo.findAllCartItemsByCustomer(username);
        Customer customer = customerRepo.findById(username).get();

        // Creating an empty order and setting values from cart
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("PENDING PAYMENT");
        order.setDiscountCode(cart.getDiscountCode());
        order.setCreatedAt(LocalDateTime.now());

        // Building orderItems with cartItems
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
        order.setSubtotal(cart.getSubtotal());
        order.setDiscountTotal(cart.getDiscountTotal());
        order.setTaxTotal(cart.getTaxTotal());
        order.setGrandTotal(cart.getGrandTotal());

        // finally persist the newly created ordeer
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
