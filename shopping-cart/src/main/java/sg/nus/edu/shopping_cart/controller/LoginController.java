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
        return "redirect:https://localhost:5173/login";
    }

    @PostMapping("/login")
    public String loginAuth(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        // check username, if customer exists
        Customer customer = customerService.getCustomerByUsername(username);
        if (customer == null) {
            // If no customer found associated with the username, bounce back to login page
            return "redirect:/login";
        }

        // check password, retrieve stored password inside database
        String storedPassword = customer.getPassword();
        if (!storedPassword.equals(password)) {
            // if store password != input password, bounce back to login page
            return "redirect:/login";
        }

    // By this point, customer exists and password matches
    // set attribute to session for future use
    session.setAttribute("username", username.toLowerCase());
    // After login, redirect to the frontend SPA catalogue
    return "redirect:https://localhost:5173/catalogue";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        return "redirect:/login";
    }
}