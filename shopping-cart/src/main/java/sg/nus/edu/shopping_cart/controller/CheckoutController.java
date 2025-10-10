package sg.nus.edu.shopping_cart.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

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
    public String displayCheckout(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        Optional<Customer> customer = customerService.findCustomerByUsername(username);
        if (customer.isEmpty()) {
            return "redirect:/login";
        }

        // by this point customer exists and has been validated
        Customer activeCustomer = customer.get();
        // persist new order with order items from cart and its cart Items
        Order order = orderService.createOrderFromCart(username);

        model.addAttribute("customerOrderItems", orderService.findOrderItemByUsername(username));
        model.addAttribute("customer", activeCustomer);
        model.addAttribute("cartTotal", order.getGrandTotal());
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
        if (customer.isEmpty()) {
            return "redirect:/login";
        }
        Optional<Order> orderOpt = orderService.findOrderByUsername(username);
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
            orderService.findOrderByUsername(username)
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
