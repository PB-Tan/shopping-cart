package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private double grandTotal;

    private String providerProduct;
    private String provider;
    private String providerTransactionId;
    private String currency;
    private String charged;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private boolean isDefault;
    private String paymentType;

    public Transaction() {
    }
}
