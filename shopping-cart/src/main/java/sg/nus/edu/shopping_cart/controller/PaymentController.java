// package sg.nus.edu.shopping_cart.controller;

// ========================combined with checkout controller
// ======================================
// serves as archive

// import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;

// import java.util.*;

// import org.springframework.stereotype.Controller;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// import sg.nus.edu.shopping_cart.dto.PaymentForm;
// import sg.nus.edu.shopping_cart.dto.PaymentSummary;
// import sg.nus.edu.shopping_cart.model.*;
// import sg.nus.edu.shopping_cart.service.*;

// @Controller
// @RequiredArgsConstructor
// public class PaymentController {

// private CartService cartService;
// private CustomerService customerService;
// private OrderService orderService;

// @GetMapping("/payment")
// public String showPaymentPage(HttpSession session, Model model) {
// String username = (String) session.getAttribute("username");

// List<CartItem> cartItems = cartService.getCartItemsByCustomer(username);
// double total = cartItems.stream()
// .mapToDouble(i -> i.getProduct().getUnitPrice() * i.getQuantity())
// .sum();

// model.addAttribute("cartItems", cartItems);
// model.addAttribute("cartTotal", total);
// return "payment"; // Bhava’s payment.html
// }

// @PostMapping("/process-payment")
// @Transactional
// public String processPayment(
// @RequestParam String cardholderName,
// @RequestParam String cardNumber,
// @RequestParam String expiry,
// @RequestParam String cvv,
// HttpSession session) {

// String username = (String) session.getAttribute("username");
// Customer customer = customerService.findCustomerByUsername(username).get();

// // Create order from cart
// List<CartItem> cartItems = cartService.getCartItemsByCustomer(username);
// double total = 0;
// for (CartItem item : cartItems) {
// total += item.getUnitPrice() * item.getQuantity();
// }

// Order order = orderService.createOrderFromCart(customer, cartItems, total);

// // Mock payment success (replace with real gateway later)
// order.setStatus("PAID");
// order.setPaymentBrand(detectBrand(cardNumber));
// order.setPaymentLast4(getLast4(cardNumber));
// order.setPaymentTxnRef("TXN-" + System.currentTimeMillis());
// orderService.save(order);

// // Clear cart
// cartService.clearCart(username);

// return "redirect:/paymentsuccess";
// }

// @GetMapping("/paymentsuccess")
// public String paymentSuccess() {
// return "paymentsuccess";
// }

// // Small private helpers (no need for DTOs)
// private String getLast4(String cardNumber) {
// if (cardNumber == null)
// return "0000";
// String digits = cardNumber.replaceAll("\\D", "");
// return digits.length() >= 4 ? digits.substring(digits.length() - 4) : "0000";
// }

// private String detectBrand(String cardNumber) {
// if (cardNumber == null)
// return "CARD";
// if (cardNumber.startsWith("4"))
// return "VISA";
// if (cardNumber.startsWith("5"))
// return "MASTERCARD";
// if (cardNumber.startsWith("3"))
// return "AMEX";
// return "CARD";
// }
// }
