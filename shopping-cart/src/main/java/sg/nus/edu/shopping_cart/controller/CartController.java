package sg.nus.edu.shopping_cart.controller;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.repository.*;
import sg.nus.edu.shopping_cart.interfaces.*;

@Controller
public class CartController {
    @Autowired
    private CartInterface cartInterface;

    @Autowired
    private CustomerInterface customerInterface;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        Customer activeCustomer = customerInterface.findCustomerByUsername(username).get();
        Cart cart = cartInterface.getCartByCustomer(username);
        BigDecimal subtotal = cartInterface.calculateCartTotal(username);
        List<CartItem> items = cartInterface.getCartItemsByCustomer(username);

        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/add")
    @Transactional
    public String addToCart(
            @RequestParam("productId") int productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        Customer activeCustomer = customerInterface.findCustomerByUsername(username).get();
        Optional<Product> productOpt = cartInterface.findProduct(productId);
        if (!productOpt.isPresent() || quantity <= 0) {
            return "redirect:/test";
        }

        cartInterface.addProductToCart(username, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    @Transactional
    public String updateCart(
            @RequestParam("productId") int productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        Customer activeCustomer = customerInterface.findCustomerByUsername(username).get();
        Optional<Product> productOpt = cartInterface.findProduct(productId);
        if (!productOpt.isPresent() || quantity <= 0) {
            return "redirect:/cart";
        }

        cartInterface.updateCartItem(username, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam int productId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        cartInterface.deleteCartItem(username, productId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        String username = (String) session.getAttribute("username");
        cartInterface.clearCart(username);
        return "redirect:/cart";
    }

}
