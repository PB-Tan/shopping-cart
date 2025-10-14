package sg.nus.edu.shopping_cart.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sg.nus.edu.shopping_cart.model.Review;
import sg.nus.edu.shopping_cart.service.ReviewService;

import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Submit review
    @PostMapping("/add")
    public String addReview(
            @RequestParam("productId") int productId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check if user is logged in
        String username = (String) session.getAttribute("username");
        if (username == null) {
            // If not logged in, redirect to login page and set the return URL
            session.setAttribute("redirectAfterLogin", "/catalogue/" + productId);
            return "redirect:/login";
        }

        try {
            // Validate rating range
            if (rating == null || rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5");
                return "redirect:/catalogue/" + productId;
            }

            // Add review
            reviewService.addReview(productId, username, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/catalogue/" + productId;
    }

    // View all reviews for the current user
    @GetMapping("/my-reviews")
    public String myReviews(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        List<Review> userReviews = reviewService.getUserReviews(username);
        model.addAttribute("reviews", userReviews);
        model.addAttribute("username", username);

        return "my-reviews";
    }

    // Delete review
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "returnTo", defaultValue = "product") String returnTo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        try {
            // Verify the review belongs to the current user
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                redirectAttributes.addFlashAttribute("error", "Review does not exist");
            } else {
                // Debug info: print username comparison
                String reviewUsername = review.getCustomer() != null ? review.getCustomer().getUsername() : "NULL";
                System.out.println("DEBUG - Session username: [" + username + "]");
                System.out.println("DEBUG - Review username: [" + reviewUsername + "]");
                System.out.println("DEBUG - Are they equal? " + reviewUsername.equals(username));

                if (review.getCustomer() == null) {
                    redirectAttributes.addFlashAttribute("error", "Review's customer information is missing");
                } else if (!review.getCustomer().getUsername().equals(username)) {
                    redirectAttributes.addFlashAttribute("error", "You can only delete your own reviews (Session: "
                            + username + ", Review: " + reviewUsername + ")");
                } else {
                    reviewService.deleteReview(reviewId);
                    redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print full exception stack trace
            redirectAttributes.addFlashAttribute("error", "failed to delete review: " + e.getMessage());
        }

        // Redirect based on originating page
        if ("myreviews".equals(returnTo)) {
            return "redirect:/review/my-reviews";
        } else if (productId != null) {
            return "redirect:/catalogue/" + productId;
        } else {
            return "redirect:/review/my-reviews";
        }
    }
}
