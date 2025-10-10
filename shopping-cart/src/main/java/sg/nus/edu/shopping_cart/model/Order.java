package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
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

    private String orderStatus;
    private String paymentStatus;
    private String fulfilmentStatus;
    private String promoCodes;
    private LocalDateTime createdAt;
    private double subTotal;
    private double taxTotal;
    private double discountTotal;
    private double grandTotal;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

}
