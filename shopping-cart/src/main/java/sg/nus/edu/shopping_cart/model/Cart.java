package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToOne // owner of FK
    @JoinColumn(name = "customer_username")
    private Customer customer;
    private BigDecimal subtotal;
    private BigDecimal grandTotal;
    private BigDecimal discountTotal;
    private BigDecimal taxTotal;
    private String discountCode;

    public Cart() {
    }
}
