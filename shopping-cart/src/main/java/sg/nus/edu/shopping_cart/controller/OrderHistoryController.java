package sg.nus.edu.shopping_cart.controller;

import org.springframework.stereotype.Controller;

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
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.validator.*;

@RequestMapping("/order/history")
@Controller
public class OrderHistoryController {

    @Autowired
    OrderInterface orderInterface;

    @Autowired
    ReviewService reviewService;

    @GetMapping("")
    public String displayOrderHistory(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        List<Order> orders = orderInterface.findAllOrdersByUsername(username);
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/{id}")
    public String displayOrderItems(HttpSession session, Model model,
            @PathVariable int id) {
        String username = (String) session.getAttribute("username");
        List<OrderItem> orderItems = orderInterface.findOrderItemByOrderId(id);
        model.addAttribute("orderItems", orderItems);

        // Check review status for each product
        Map<Integer, Boolean> reviewStatusMap = new HashMap<>();
        if (username != null) {
            for (OrderItem item : orderItems) {
                int productId = item.getProduct().getId();
                boolean hasReviewed = reviewService.hasUserReviewed(productId, username);
                reviewStatusMap.put(productId, hasReviewed);
            }
        }
        model.addAttribute("reviewStatusMap", reviewStatusMap);

        return "order-details";
    }

    // @GetMapping("/{id}/review")
    // public String displayReviewForm(HttpSession session, Model model,
    // @PathVariable int id) {
    // String username = (String) session.getAttribute("username");
    // model.addAttribute("username", username);
    // return "order-review";
    // }
}
