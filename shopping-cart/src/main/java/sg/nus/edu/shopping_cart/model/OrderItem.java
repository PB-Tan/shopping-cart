package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Getter
@Setter
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String productName;
    private double unitPrice;
    private int quantity;
    private double itemTotal; // derivable

    public OrderItem() {
    }

    @ManyToOne
    private Product product;

    @ManyToOne
    private Order order;
}
