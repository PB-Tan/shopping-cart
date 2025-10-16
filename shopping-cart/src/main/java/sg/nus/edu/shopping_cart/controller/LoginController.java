package sg.nus.edu.shopping_cart.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.service.CustomerService;

@Controller
public class LoginController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/login")
    public String displayLogin() {
        // In this app, the React frontend handles the login UI/routing.
        // Redirect requests for /login to the frontend root so the React router
        // can display the client-side Login page.
        return "redirect:/";
    }

    @PostMapping("/login")
    public String loginAuth(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {
        // Server-side Thymeleaf form-login is deprecated in this project.
        // The React frontend should call the REST endpoint
        // POST /api/customers/customerlogin to perform authentication.
        // Redirect to frontend root (React app) so client-side login flow runs.
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        return "redirect:/login";
    }
}