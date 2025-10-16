// edit by serene
package sg.nus.edu.shopping_cart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sg.nus.edu.shopping_cart.model.Product;
import sg.nus.edu.shopping_cart.service.FavoriteService;

import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * Toggle favorite status (form submission)
     */
    @PostMapping("/toggle")
    public String toggleFavorite(
            @RequestParam Integer productId,
            @RequestParam(required = false) String returnUrl,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            // User not logged in, redirect to login (frontend dev server over HTTPS)
            return "redirect:https://localhost:5173/login";
        }

        // Toggle the favorite
        favoriteService.toggleFavorite(username, productId);

        return "redirect:" + returnUrl;
       
    }

    /**
     * Page to view all favorites with optional sorting
     */
    @GetMapping("")
    public String viewFavorites(
            @RequestParam(required = false) String sortBy,
            HttpSession session,
            Model model) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        List<Product> favoriteProducts;
        if (sortBy != null && !sortBy.isEmpty()) {
            favoriteProducts = favoriteService.getUserFavoriteProductsSorted(username, sortBy);
        } else {
            favoriteProducts = favoriteService.getUserFavoriteProducts(username);
        }

        model.addAttribute("productlist", favoriteProducts);
        model.addAttribute("pageTitle", "My Favorites");
        model.addAttribute("currentSort", sortBy != null ? sortBy : "");

        return "favorites";
    }
}
