package sg.nus.edu.shopping_cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.model.SearchLog;
import sg.nus.edu.shopping_cart.service.SearchLogService;

@Controller
public class SearchHistoryController {

    @Autowired
    private SearchLogService searchLogService;

    @GetMapping("/search/history")
    public String history(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        List<SearchLog> logs = searchLogService.findRecentByUsername(username);
        model.addAttribute("logs", logs);
        model.addAttribute("username", username);
        return "search-history";
    }
}

