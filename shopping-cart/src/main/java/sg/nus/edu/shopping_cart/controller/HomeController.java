// src/main/java/sg/nus/edu/shopping_cart/controller/HomeController.java
package sg.nus.edu.shopping_cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // When user visits "/", send them to "/catalogue"
    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/catalogue";
    }
}
