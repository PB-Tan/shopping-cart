package sg.nus.edu.shopping_cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.nus.edu.shopping_cart.model.*;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {

    @Query("SELECT dc.percent FROM DiscountCode dc WHERE dc.code = :code")
    public Optional<Double> findPercentByCode(@Param("code") String code);
}
