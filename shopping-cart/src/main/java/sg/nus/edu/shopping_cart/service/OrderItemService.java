package sg.nus.edu.shopping_cart.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.repository.*;

@Service
@Transactional(readOnly = true)
public class OrderItemService implements OrderItemInterface {

    @Autowired
    OrderItemRepository oir;

    @Autowired
    OrderService os;

    @Override
    public OrderItem findOrderItemById(int id) {
        return oir.findById(id).get();
    }

    @Transactional(readOnly = false)
    public OrderItem saveOrderItem(OrderItem orderItem) {
        oir.save(orderItem);
        return orderItem;
    }

    public List<OrderItem> findActiveOrderItemsByUsername(String username) {
        Optional<Order> order = os.findActiveOrderByUsername(username);
        if (order.isEmpty()) {
            return null;
        }
        // active order found
        Order activeOrder = order.get();
        List<OrderItem> orderItems = oir.findAllByOrderId(activeOrder.getId());
        return orderItems;

    }
}
