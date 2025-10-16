package sg.nus.edu.shopping_cart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.model.Product;
import sg.nus.edu.shopping_cart.model.Review;
import sg.nus.edu.shopping_cart.repository.CustomerRepository;
import sg.nus.edu.shopping_cart.repository.ProductRepository;
import sg.nus.edu.shopping_cart.repository.ReviewRepository;
import sg.nus.edu.shopping_cart.repository.OrderItemRepository;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // 添加评价
    public Review addReview(int productId, String username, Integer rating, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product does not exist"));

        Customer customer = customerRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        // 检查用户是否已经评价过该商品
        Review existingReview = reviewRepository.findByProductIdAndCustomerUsername(productId, username);
        if (existingReview != null) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    // 获取商品的所有评价
    public List<Review> getProductReviews(int productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // 获取用户的所有评价
    public List<Review> getUserReviews(String username) {
        return reviewRepository.findByCustomerUsername(username);
    }

    // 获取商品的平均评分
    public Double getAverageRating(int productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // 获取商品的评价总数
    public long getReviewCount(int productId) {
        return reviewRepository.countByProductId(productId);
    }

    // 检查用户是否已经评价过该商品
    public boolean hasUserReviewed(int productId, String username) {
        return reviewRepository.findByProductIdAndCustomerUsername(productId, username) != null;
    }

    // 根据ID获取评价
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    // 删除评价
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    // Check if user has purchased the product
    public boolean hasUserPurchasedProduct(int productId, String username) {
        if (username == null) {
            return false;
        }
        return orderItemRepository.hasUserPurchasedProduct(productId, username);
    }
}
