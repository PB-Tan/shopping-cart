package sg.nus.edu.shopping_cart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import sg.nus.edu.shopping_cart.model.Order;
import sg.nus.edu.shopping_cart.model.OrderItem;
import sg.nus.edu.shopping_cart.model.StripeResponse;
import sg.nus.edu.shopping_cart.repository.DiscountCodeRepository;

@Service
public class StripeService {
    @Value("${stripe.secret-key}")
    private String secretKey;

    @Autowired
    DiscountCodeRepository discountCodeRepository;

    // stripe -API
    // -> productName, amount, quantity, currency -> only need these 4 inputs to
    // connect to stripe api payment gateway
    // -> will return sessionId and checkout url

    public StripeResponse payProducts(List<OrderItem> orderItems, Order order) {
        Stripe.apiKey = secretKey;

        if (orderItems == null || orderItems.isEmpty()) {
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Cart is empty")
                    .build();
        }

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            // get the four required parameters for stripe api
            Long amount = Math.round(orderItem.getUnitPrice() * 100); // need to change double to long for api
            Long quantity = (long) orderItem.getQuantity(); // need to change quantity to long as well

            // Product name
            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                    .builder()
                    .setName(orderItem.getProductName()).build();

            // Price of item and currency
            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("sgd")
                    .setUnitAmount(amount)
                    .setProductData(productData)
                    .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE)
                    .build();
            // Quantity
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(quantity)
                    .setPriceData(priceData)
                    .addTaxRate("txr_1SHP8CLE4e5BDmJr8uRGOfvB")
                    .build();

            lineItems.add(lineItem);
        }
        // Create Stripe session
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putMetadata("orderId", String.valueOf(order.getId()))
                .setSuccessUrl("http://localhost:8080/checkout/success?session-id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/cart")
                .addAllLineItem(lineItems)
                .setAutomaticTax(SessionCreateParams.AutomaticTax.builder()
                        .setEnabled(false)
                        .build());

        // Implement discount from discount code to final price //
        // String discountCode = order.getDiscountCode();
        // Optional<Double> discountPercent =
        // discountCodeRepository.findPercentByCode(discountCode);
        // if (discountPercent.isPresent()) {
        // discountPercent.get();

        BigDecimal discount = BigDecimal.ZERO;
        if (order.getDiscountTotal() != null) {
            discount = order.getDiscountTotal();
        }
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            Long discountAmountInCents = discount
                    .setScale(2, RoundingMode.HALF_UP) // ensure 2dp
                    .movePointRight(2) // *100 to cents
                    .longValueExact(); // round up if not whole cent

            try {
                CouponCreateParams couponParams = CouponCreateParams.builder()
                        .setName("Cart Discount")
                        .setCurrency("sgd")
                        .setAmountOff(discountAmountInCents)
                        .setDuration(CouponCreateParams.Duration.ONCE)
                        .build();

                Coupon coupon = Coupon.create(couponParams);
                sessionBuilder
                        .addDiscount(SessionCreateParams.Discount.builder().setCoupon(coupon.getId()).build());
            } catch (StripeException e) {
                e.printStackTrace();
                return StripeResponse.builder()
                        .status("FAILED")
                        .message("Could not create discount coupon" + e.getMessage())
                        .build();
            }
        }

        Session session = null;

        try {
            session = Session.create(sessionBuilder.build());
            System.out.println("[STRIPE] sessionURL=" + session.getUrl());
            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        } catch (StripeException ex) {
            // log
            ex.printStackTrace();
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Stripe session creation failed: " + ex.getMessage())
                    .build();
        }
    }
}
