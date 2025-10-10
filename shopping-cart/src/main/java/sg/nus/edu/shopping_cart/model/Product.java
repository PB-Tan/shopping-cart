package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 50)
    private String description;

    @Column(length = 20)
    private String name;

    private double unitPrice;

    private int stock;
    private String category;
    private String brand;
    private String collection;
    private String imageUrl;
    private String imageAlt;

    // @JsonIgnore
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    public Product() {
    }
}
