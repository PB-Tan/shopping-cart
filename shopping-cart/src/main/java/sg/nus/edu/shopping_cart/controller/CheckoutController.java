package sg.nus.edu.shopping_cart.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.service.*;

@Controller
// @RequestMapping("")
public class CheckoutController {

    @Autowired
    CartService cartService;

    @Autowired
    CustomerService cs;

    @Autowired
    OrderService os;

    @Autowired
    ShipmentService ss;

    @Autowired
    OrderItemService ois;

    String activeUsername = "";
    Optional<Customer> customer;

    @GetMapping("/checkout")
    public String displayCheckout(HttpSession session, Model model) {
        activeUsername = (String) session.getAttribute("username");
        // if (activeUsername == null) {
        // // optional: session.setAttribute("error", "Not authenticated");
        // return "redirect:/login";
        // }

        customer = cs.findCustomerByUsername(activeUsername);
        if (customer.isEmpty()) {
            return "redirect:/login";
        }

        // by this point customer exists and has been validated
        Customer activeCustomer = customer.get();
        List<CartItem> items = cartService.getCartItemsByCustomer(activeUsername);
        double cartTotal = 0;
        for (CartItem cartItem : items) {
            cartTotal += cartItem.getUnitPrice() * cartItem.getQuantity();
            // persist cartitems into orderItems
            OrderItem orderItem = new OrderItem();
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setItemDiscount(0);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setItemTax(0);
            orderItem.setItemTotal(cartItem.getUnitPrice() * cartItem.getQuantity() // - discount + tax
            );
            ois.saveOrderItem(orderItem);
        }

        model.addAttribute("customerCartItems", items);
        model.addAttribute("customer", activeCustomer);
        model.addAttribute("cartTotal", cartTotal);
        return "checkout";
    }

    // Persist cart items into a new ACTIVE order for the logged-in customer
    @PostMapping("/checkout/billing")
    public String confirmCheckout(
            HttpSession session,
            @RequestParam String cardNumber,
            @RequestParam Integer expMonth,
            @RequestParam Integer expYear,
            @RequestParam String cvv) {

        // // check if session is valid
        // if (activeUsername == null) {
        // return "redirect:/login";
        // }

        Optional<Order> orderOpt = os.createActiveOrderFromCart(activeUsername);
        if (orderOpt.isEmpty()) {
            return "redirect:/checkout";
        }
        Order activeOrder = orderOpt.get();
        List<OrderItem> orderItems = ois.findActiveOrderItemsByUsername(activeUsername);
        for (OrderItem orderItem : orderItems) {

        }

        return "checkout/confirm";

    }

    @PostMapping("/checkout/shipping")
    public String submitShippingMethod(@RequestParam("shippingMethod") String shippingMethod,
            HttpSession session,
            Model model) {

        // if (activeUsername == null) {
        // return "redirect:/login";
        // }

        os.updateShippingMethodForActiveOrder(activeUsername, shippingMethod);
        return "redirect:/checkout";
    }

}
