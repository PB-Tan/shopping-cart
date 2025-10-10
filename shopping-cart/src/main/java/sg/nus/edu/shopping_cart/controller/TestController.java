package sg.nus.edu.shopping_cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.interfaces.*;

@Controller
public class TestController {
    @Autowired
    CustomerInterface ci;

    @GetMapping("/test")
    public String displayLoginSuccess(
            HttpSession session,
            Model model) {
        String activeUsername = (String) session.getAttribute("username");
        Customer activeCustomer = ci.findCustomerByUsername(activeUsername).get();
        model.addAttribute("greetCustomer", activeCustomer);
        return "success";
    }
}
