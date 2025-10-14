package sg.nus.edu.shopping_cart.controller;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.service.*;
import sg.nus.edu.shopping_cart.validator.*;

@Controller
// @RequestMapping("")
public class CheckoutController {

    @Autowired
    CartService cartService;

    @Autowired
    CustomerService customerService;

    @Autowired
    OrderService orderService;

    @Autowired
    ShipmentService shipmentService;

    @Autowired
    private PaymentMethodValidator paymentMethodValidator;

    @InitBinder("paymentMethod")
    private void initPaymentMethodValidator(WebDataBinder binder) {
        binder.addValidators(paymentMethodValidator);
    }

    // checkout page from cart
    @GetMapping("/checkout")
    public String displayCheckout(HttpSession session, Model model, RedirectAttributes ra) {
        String username = (String) session.getAttribute("username");
        // assuming that customer has been validated, due to security interceptor
        Customer customer = customerService.findCustomerByUsername(username).get();
        List<CartItem> cartItems = customer.getCart().getCartItems();
        // if cartItem is out of stock, then prevent customer from going into payment
        // page, and show error msg
        for (CartItem cartItem : cartItems) {
            int stock = cartItem.getProduct().getStock();
            int qty = cartItem.getQuantity();
            if (qty > stock) {
                String msg = cartItem.getProduct().getName() + " quantity cannot be higher than stock: " + stock;
                ra.addFlashAttribute("errorMsg", msg);
                return "redirect:/cart";
            }
        }

        // if order is empty, then prevent going into payment page and show error msg
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMsg", "Cart cannot empty");
            return "cart";
        }

        // if no active order made for current cart, persist new order with order items
        // from cart and its cart Items
        if (orderService.findTopOrderByUsername(username).isEmpty()) {
            Order order = orderService.createOrderFromCart(username);
            List<OrderItem> orderItems = orderService.findOrderItemByUsername(username);
            BigDecimal cartTotal = cartService.calculateCartTotal(username);
            model.addAttribute("customerOrderItems", orderItems);
            model.addAttribute("customer", customer);
            model.addAttribute("cartTotal", cartTotal);
        }
        return "payment";
    }

    // select payment methods
    @PostMapping("/checkout/method")
    public String selectPaymentMethod(
            HttpSession session,
            @RequestParam String paymentMethod) {
        if (paymentMethod.equals("card")) {
            return "redirect:/checkout/creditcard";
        } else {
            return "redirect:/checkout/qr";
        }
    }

    @GetMapping("/checkout/creditcard")
    public String displayCreditCardForm(Model model) {
        if (!model.containsAttribute("paymentMethod")) {
            model.addAttribute("paymentMethod", new PaymentMethod());
        }
        return "credit-card-payment";
    }

    // When customer clicks pay with credit card
    @PostMapping("/checkout/creditcard")
    public String payWithCreditCard(
            HttpSession session,
            @Validated @ModelAttribute("paymentMethod") PaymentMethod paymentMethod,
            BindingResult bindingResult,
            Model model) {

        // validate credentials
        if (bindingResult.hasErrors()) {
            return "credit-card-payment";
        }

        String username = (String) session.getAttribute("username");
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        Optional<Order> orderOpt = orderService.findTopOrderByUsername(username);
        if (orderOpt.isEmpty()) {
            return "redirect:/error";
        }
        Order order = orderOpt.get();
        order.setStatus("PENDING");
        model.addAttribute("customer", customer.get());
        model.addAttribute("cartTotal", orderOpt.get().getGrandTotal());

        return "redirect:/checkout/success";
    }

    @GetMapping("/checkout/success")
    public String paymentSuccess(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        // Unwrap Optional<Customer> before placing into the model
        model.addAttribute("customer", customerService.findCustomerByUsername(username).get());
        Optional<Order> order = orderService.findTopOrderByUsername(username);
        if (order.isPresent()) {
            order.get().setStatus("PAID");
            cartService.clearCart(username);
        }
        return "paymentsuccess";
    }

    // @PostMapping("/checkout/shipping")
    // public String submitShippingMethod(
    // @RequestParam("shippingMethod") String shippingMethod,
    // HttpSession session,
    // Model model) {

    // String username = (String) session.getAttribute("username");
    // orderService.updateShippingMethodForOrder(username, shippingMethod);
    // return "redirect:/checkout";
    // }

}
