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

    // 提交评价
    @PostMapping("/add")
    public String addReview(
            @RequestParam("productId") int productId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 检查用户是否登录
        String username = (String) session.getAttribute("username");
        if (username == null) {
            // 如果未登录，重定向到登录页面，并设置返回URL
            session.setAttribute("redirectAfterLogin", "/catalogue/" + productId);
            return "redirect:/login";
        }

        try {
            // 验证评分范围
            if (rating == null || rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5");
                return "redirect:/catalogue/" + productId;
            }

            // 添加评价
            reviewService.addReview(productId, username, rating, comment);
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/catalogue/" + productId;
    }

    // 查看用户所有评价
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

    // 删除评价
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
            // 验证评价是否属于当前用户
            Review review = reviewService.getReviewById(reviewId);
            if (review == null) {
                redirectAttributes.addFlashAttribute("error", "Review does not exist");
            } else {
                // 调试信息：打印用户名比较
                String reviewUsername = review.getCustomer() != null ? review.getCustomer().getUsername() : "NULL";
                System.out.println("DEBUG - Session username: [" + username + "]");
                System.out.println("DEBUG - Review username: [" + reviewUsername + "]");
                System.out.println("DEBUG - Are they equal? " + reviewUsername.equals(username));

                if (review.getCustomer() == null) {
                    redirectAttributes.addFlashAttribute("error", "Review's customer information is missing");
                } else if (!review.getCustomer().getUsername().equals(username)) {
                    redirectAttributes.addFlashAttribute("error", "You can only delete your own reviews (Session: " + username + ", Review: " + reviewUsername + ")");
                } else {
                    reviewService.deleteReview(reviewId);
                    redirectAttributes.addFlashAttribute("success", "Review deleted successfully");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印完整的异常堆栈
            redirectAttributes.addFlashAttribute("error", "failed to delete review：" + e.getMessage());
        }

        // 根据来源返回不同页面
        if ("myreviews".equals(returnTo)) {
            return "redirect:/review/my-reviews";
        } else if (productId != null) {
            return "redirect:/catalogue/" + productId;
        } else {
            return "redirect:/review/my-reviews";
        }
    }
}
