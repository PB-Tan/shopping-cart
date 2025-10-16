package sg.nus.edu.shopping_cart.controller;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Cart cart = cartInterface.getCartByCustomer(username);
        BigDecimal subtotal = cartInterface.calculateCartSubtotal(username);
        List<CartItem> items = cartInterface.getCartItemsByCustomer(username);
        BigDecimal grandtotal = cartInterface.calculateCartGrandTotal(username);

        model.addAttribute("items", items);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/discountCode")
    @Transactional
    public String calculateGrandTotal(HttpSession session,
            RedirectAttributes ra,
            @RequestParam("discountCode") String discountCode,
            Model model) {
        // ov test
        discountCode = discountCode.toUpperCase().trim();
        String username = (String) session.getAttribute("username");
        Cart cart = cartInterface.getCartByCustomer(username);
        BigDecimal subtotal = cartInterface.calculateCartSubtotal(username);
        BigDecimal grandTotal;

        // check if discount code is valid.
        if (cartInterface.getPercentByCode(discountCode).isPresent()) {
            cart.setDiscountCode(discountCode);
            BigDecimal discountpercent = BigDecimal
                    .valueOf(cartInterface.getPercentByCode(cart.getDiscountCode()).get());
            cart.setDiscountTotal(subtotal.multiply(discountpercent).divide(BigDecimal.valueOf(100)));
            ra.addFlashAttribute("codeApplied", discountCode.toUpperCase() + " has been successfully applied");
            cartInterface.saveCart(cart);
        } else {
            grandTotal = subtotal;
            ra.addFlashAttribute("invalidCode", "Discount code not found");
            cart.setGrandTotal(grandTotal);
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/add")
    @Transactional
    public String addToCart(
            @RequestParam("productId") int productId,
            @RequestParam("quantity") int quantity,
            RedirectAttributes ra,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        Product product = cartInterface.findProduct(productId).get();
        Cart cart = cartInterface.getCartByCustomer(username);
        int stock = product.getStock();

        // check if item already exist inside cart, if not, set as null
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null); // if no return null

        // first sanity check if the quantity makes sense
        if (quantity <= 0) {
            ra.addFlashAttribute("errorMsg", "Quantity must be at least 1");
            ra.addAttribute("id", productId);
            return "redirect:/catalogue/" + productId;
        } // if product is not found in cart, then does the qty exceed stock?
        if (existingItem == null && quantity > stock) {
            ra.addFlashAttribute("errorMsg", "Only " + stock + " left for " + product.getName());
            return "redirect:/catalogue/" + productId;
        } // if product is found inside cart, then does the combined qty exceed stock?
        if (existingItem != null && quantity + existingItem.getQuantity() > stock) {
            ra.addFlashAttribute("errorMsg",
                    "Only " + stock + " left for " + product.getName() + ". You have " + existingItem.getQuantity()
                            + " in your cart");
            return "redirect:/catalogue/" + productId;
        }

        // by this point, quantity to be added to cart has been validated.
        cartInterface.addProductToCart(username, productId, quantity);
        ra.addFlashAttribute("statusMsg", quantity + " " + product.getName() + " has been successfully added to cart");
        return "redirect:/catalogue/" + productId;
    }

    @PostMapping("/cart/update")
    @Transactional
    public String updateCart(
            @RequestParam("productId") int productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
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
