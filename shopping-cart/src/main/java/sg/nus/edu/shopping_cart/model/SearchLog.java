package sg.nus.edu.shopping_cart.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;     //pk

    private String username; //  guestuser can keep null

    private String searchby; // when productname/category

    private String keyword;   //mouse/keyboard

    @Column(length = 2000)
    private String cartSnapshot; // simple text snapshot of cart items

    private Integer resultCount; // number of products returned by the search

    private LocalDateTime createdAt;

    @PrePersist  // JPA callback to set createdAt before saving in to database
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

