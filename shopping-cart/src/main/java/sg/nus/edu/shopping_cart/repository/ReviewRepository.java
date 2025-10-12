package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sg.nus.edu.shopping_cart.model.Review;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 根据商品ID查找所有评价，按创建时间降序排列
    List<Review> findByProductIdOrderByCreatedAtDesc(int productId);

    // 根据用户名查找所有评价
    List<Review> findByCustomerUsername(String username);

    // 根据商品ID和用户名查找评价（用于检查用户是否已经评价过）
    Review findByProductIdAndCustomerUsername(int productId, String username);

    // 计算商品的平均评分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") int productId);

    // 获取商品的评价总数
    long countByProductId(int productId);
}
