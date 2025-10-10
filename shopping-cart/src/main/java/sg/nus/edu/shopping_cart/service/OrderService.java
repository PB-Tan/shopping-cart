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
    OrderRepository or;

    @Autowired
    ShipmentRepository sr;

    @Autowired
    CustomerRepository cr;

    @Autowired
    CartItemRepository cir;

    @Autowired
    OrderItemRepository oir;

    @Override
    public Order findOrderById(int id) {
        return or.findById(id).get();
    }

    @Override
    public Optional<Order> findActiveOrderByUsername(String username) {
        return or.findActiveOrderByUsername(username);
    }

    @Transactional(readOnly = false)
    public void updateShippingMethodForActiveOrder(String username, String shippingMethod) {
        Optional<Order> activeOrder = or.findActiveOrderByUsername(username);
        if (activeOrder.isPresent()) {
            Order order = activeOrder.get();
            Shipment shipment = order.getShipment();

            // if no shipment has been found prior, then persist into
            // a new shipment
            // assoicated with the order
            if (shipment == null) {
                shipment = new Shipment();
                sr.save(shipment); // assigns id with
            }

            // update shipment method for new shipment
            shipment.setShipmentMethod(shippingMethod);
            order.setShipment(shipment);
            or.save(order);
        }

        // if no active orders found, then do nothing

    }

    @Transactional(readOnly = false)
    public Optional<Order> createActiveOrderFromCart(String username) {
        if (username == null || username.isBlank())
            return Optional.empty();

        Optional<Customer> customerOpt = cr.findById(username);
        if (customerOpt.isEmpty())
            return Optional.empty();

        List<CartItem> cartItems = cir.findAllCartItemsByCustomer(username);
        if (cartItems == null || cartItems.isEmpty())
            return Optional.empty();

        Order order = new Order();
        order.setCustomer(customerOpt.get());
        order.setOrderStatus("ACTIVE");
        order.setPaymentStatus("UNPAID");
        order.setFulfilmentStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        double subTotal = 0.0;
        for (CartItem ci : cartItems) {
            subTotal += ci.getUnitPrice() * ci.getQuantity();
        }
        order.setSubTotal(subTotal);
        order.setTaxTotal(0.0);
        order.setDiscountTotal(0.0);
        order.setGrandTotal(subTotal);

        order = or.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            if (ci.getProduct() != null) {
                oi.setProductName(ci.getProduct().getName());
            } else {
                oi.setProductName(null);
            }
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setItemDiscount(0);
            oi.setItemTax(0);
            oi.setQuantity(ci.getQuantity());
            oi.setItemTotal(oi.getUnitPrice() * oi.getQuantity() - oi.getItemDiscount() + oi.getItemTax());
            oir.save(oi);
            orderItems.add(oi);
        }
        order.setOrderItems(orderItems);
        or.save(order);

        cir.deleteAll(cartItems);

        return Optional.of(order);
    }

}
