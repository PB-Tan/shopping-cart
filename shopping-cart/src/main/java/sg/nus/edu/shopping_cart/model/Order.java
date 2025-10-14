package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne // owning
    @JoinColumn(name = "customer_username")
    private Customer customer;

    @OneToOne // owning
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @OneToOne // owning
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    private String status; // PENDING, PAID, FAILED, CANCELLED
    private String discountCode;
    private LocalDateTime createdAt;
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

}
