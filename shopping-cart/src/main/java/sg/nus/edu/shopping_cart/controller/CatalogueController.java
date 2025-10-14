package sg.nus.edu.shopping_cart.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.service.*;

@Controller
@RequestMapping("/catalogue")
public class CatalogueController {

    @Autowired
    private ProductInterface pi;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("")
    // 1. list all the products
    public String home(Model model, HttpSession session) {

        List<Product> products = pi.findAll(); // it will take all the products from the DB through repos
        model.addAttribute("productlist", products);

        // Add favorite status for each product
        // edit by serene
        String username = (String) session.getAttribute("username");
        if (username != null) {
            Set<Integer> favoritedProductIds = new HashSet<>();
            List<Product> favoriteProducts = favoriteService.getUserFavoriteProducts(username);
            for (Product p : favoriteProducts) {
                favoritedProductIds.add(p.getId());
            }
            model.addAttribute("favoritedProductIds", favoritedProductIds);
        } else {
            model.addAttribute("favoritedProductIds", new HashSet<Integer>());
        }

        // System.out.println("product Count: " + products.size()); // just for my test
        // purpose -->ov

        return "catalogue";
    }

    @GetMapping("/search") // to search by name or category
    public String search(
            @RequestParam("searchby") String searchby,
            @RequestParam("keyword") String keyword,
            Model model) {

        String name = new String("name");
        String category = new String("category");

        // so the final result will be added to the productlist again

        if (searchby.equals(name)) {
            model.addAttribute("products", pi.findProductByNameContainingIgnoreCase(keyword));
        } else if (searchby.equals(category)) {
            model.addAttribute("products", pi.findProductByCategoryContainingIgnoreCase(keyword));
        } else {
            return "home";
        }

        return "searchResults";

    }

    @GetMapping("/{id}")
    public String idDetail(@PathVariable int id, Model model, HttpSession session) {
        List<Product> allproducts = pi.findAll();

        Product pro = allproducts.stream()
                .filter(productls -> productls.getId() == id)
                .findFirst()
                .orElse(null);

        model.addAttribute("productlist", pro);
        int stock = pro.getStock();
        if (stock <= 0) {
            model.addAttribute("errorMsg", pro.getName() + " is out of stock");
        }

        // populate thymeleaf tags in HTML with attributes regarding products'
        // attributes
        if (pro != null) {
            // retrieve all product reviews associated with the product
            model.addAttribute("reviews", reviewService.getProductReviews(id));
            // retrieve average rating across all ratings asssociated with product
            model.addAttribute("averageRating", reviewService.getAverageRating(id));
            // retrieve total count of reviews associated with product
            model.addAttribute("reviewCount", reviewService.getReviewCount(id));

            // 检查当前用户是否已经评价过以及是否购买过该商品
            String username = (String) session.getAttribute("username");
            if (username != null) {
                model.addAttribute("hasReviewed", reviewService.hasUserReviewed(id, username));
                model.addAttribute("hasPurchased", reviewService.hasUserPurchasedProduct(id, username));
            } else {
                model.addAttribute("hasReviewed", false);
                model.addAttribute("hasPurchased", false);
            }
        }

        return "productDetails";
    }

}
