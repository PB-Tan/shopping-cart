package sg.nus.edu.shopping_cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    // Redirect root path to /catalogue
    @GetMapping("/")
    public String redirectToCatalogue() {
        return "redirect:/catalogue";
    }
}
