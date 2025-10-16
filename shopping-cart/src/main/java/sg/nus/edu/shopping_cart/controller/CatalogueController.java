package sg.nus.edu.shopping_cart.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.interfaces.ProductInterface;
import sg.nus.edu.shopping_cart.model.Product;
import sg.nus.edu.shopping_cart.service.FavoriteService;
import sg.nus.edu.shopping_cart.service.ReviewService;
import sg.nus.edu.shopping_cart.model.ProductViewLog;
import sg.nus.edu.shopping_cart.service.ProductViewLogService;
import sg.nus.edu.shopping_cart.model.SearchLog;
import sg.nus.edu.shopping_cart.service.SearchLogService;

@Controller
@RequestMapping("/catalogue")
public class CatalogueController {

    @Autowired
    private ProductInterface pi;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductViewLogService productViewLogService;

    @Autowired
    private SearchLogService searchLogService;

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
        // purpose

        return "catalogue";
    }

    @GetMapping("/search") // to search by name or category
    public String search(
            @RequestParam("searchby") String searchby,
            @RequestParam("keyword") String keyword,
            Model model,
            HttpSession session) {

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
        // persist a product view log (user or guest) only if product exists
        if (pro != null) {
            String viewer = (String) session.getAttribute("username");
            // persist product view log
            ProductViewLog viewLog = new ProductViewLog();
            viewLog.setUsername(viewer);
            viewLog.setProductId(pro.getId());
            viewLog.setProductName(pro.getName());
            productViewLogService.save(viewLog);

            // also store in SearchLog so it appears in search history
            if (viewer != null) {
                SearchLog searchLog = new SearchLog();
                searchLog.setUsername(viewer);
                searchLog.setSearchby("product");
                searchLog.setKeyword(pro.getName());
                searchLog.setCartSnapshot("view details");
                searchLog.setResultCount(1);
                searchLogService.save(searchLog);
            }
        }
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

            // 检查当前用户是否已经评价过
            String username = (String) session.getAttribute("username");
            if (username != null) {
                model.addAttribute("hasReviewed", reviewService.hasUserReviewed(id, username));
            } else {
                model.addAttribute("hasReviewed", false);
            }
        }

        return "productDetails";
    }

}

 