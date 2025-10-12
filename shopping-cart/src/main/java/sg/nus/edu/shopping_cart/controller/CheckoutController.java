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

    // payment controlling
    @GetMapping("/checkout")
    public String displayCheckout(HttpSession session, Model model, RedirectAttributes ra) {
        String username = (String) session.getAttribute("username");
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return "redirect:/login";
        }
        // by this point customer exists and has been validated
        Customer activeCustomer = customer.get();
        List<CartItem> cartItems = activeCustomer.getCart().getCartItems();
        // if cartItem is out of stock, then prevent going into payment page, and show
        // error msg
        for (CartItem cartItem : cartItems) {
            int stock = cartItem.getProduct().getStock();
            int qty = cartItem.getQuantity();
            if (qty > stock) {
                String msg = cartItem.getProduct().getName() + " is out of stock";
                ra.addFlashAttribute("errorMsg", msg);
                return "redirect:/cart";
            }
        }

        // persist new order with order items from cart and its cart Items and empty
        // cart
        Order order = orderService.createOrderFromCart(username);
        List<OrderItem> orderItems = orderService.findOrderItemByUsername(username);
        BigDecimal cartTotal = cartService.calculateCartTotal(username);
        model.addAttribute("customerOrderItems", orderItems);
        model.addAttribute("customer", activeCustomer);
        model.addAttribute("cartTotal", cartTotal);

        // if order is empty, then prevent going into payment page and show error msg
        if (order.getGrandTotal() == 0) {
            model.addAttribute("errorMsg", "cart is empty");
            return "cart";
        }

        else {
            return "payment";
        }
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

    @GetMapping("checkout/creditcard")
    public String displayCreditCardForm(Model model) {
        if (!model.containsAttribute("paymentMethod")) {
            model.addAttribute("paymentMethod", new PaymentMethod());
        }
        return "credit-card-payment";
    }

    @PostMapping("/checkout/creditcard")
    public String payWithCreditCard(
            HttpSession session,
            @Validated @ModelAttribute("paymentMethod") PaymentMethod paymentMethod,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "credit-card-payment";
        }

        String username = (String) session.getAttribute("username");
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        Optional<Order> orderOpt = orderService.findTopOrderByUsername(username);
        if (orderOpt.isEmpty()) {
            return "redirect:/test";
        }

        model.addAttribute("customer", customer.get());
        model.addAttribute("cartTotal", orderOpt.get().getGrandTotal());

        return "redirect:/checkout/success";
    }

    @GetMapping("/checkout/success")
    public String displaySuccess(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            customerService.findCustomerByUsername(username).ifPresent(c -> model.addAttribute("customer", c));
            orderService.findTopOrderByUsername(username)
                    .ifPresent(o -> model.addAttribute("cartTotal", o.getGrandTotal()));
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
