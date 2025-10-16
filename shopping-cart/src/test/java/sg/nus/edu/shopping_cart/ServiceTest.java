package sg.nus.edu.shopping_cart;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.repository.*;
import sg.nus.edu.shopping_cart.service.*;

@SpringBootTest
public class ServiceTest {

    @Autowired
    CartService cartService;

    @Autowired
    OrderService orderService;

    @Test
    public void testSubtotalWithCartItems () {
        BigDecimal testSubtotal = cartService.calculateCartSubtotal("alice");

        BigDecimal expectedSubtotal = //29.9 * 2 + 49.90
            BigDecimal.valueOf(29.90).multiply(BigDecimal.valueOf(2)).add(BigDecimal.valueOf(49.90));
        assertEquals(0, testSubtotal.compareTo(expectedSubtotal));
    }

    @Test
    public void testDiscountValidity_GrandTotalReduction(){
        BigDecimal preDiscountGrandTotal = cartService.calculateCartGrandTotal("alice");
        Cart cart = cartService.getCartByCustomer("alice");
        cart.setDiscountCode("TEAM08");
        BigDecimal postDiscountGrandTotal = cartService.calculateCartGrandTotal("alice");

        //BigDecimal expectedDiscount = BigDecimal.valueOf(0.50);
        assertTrue(cart.getDiscountTotal().compareTo(BigDecimal.ZERO) > 0); //check if discount total was applied
        assertTrue(postDiscountGrandTotal.compareTo(preDiscountGrandTotal) < 0);  //check if post-discount is smaller than pre
    }

    @Test
    public void testOrderCreation () {
        Order testOrder = orderService.createOrderFromCart("alice");
        List<OrderItem> testOrderItems = testOrder.getOrderItems();
        List<Order> verifyOrder = orderService.findAllOrdersByUsername("alice");
        
        assertNotNull(testOrder);
        assertTrue(verifyOrder.contains(testOrder));
        assertTrue(testOrderItems.size() == orderService.findOrderItemByUsername("alice").size());
    }

}
