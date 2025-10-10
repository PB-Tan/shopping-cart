package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "customer_username")
    private Customer customer;

    private int expiryYear;
    private int expiryMonth;
    private String cardType;
    private String cardHolderName;
    private String lastFourDigits;
    private boolean isDefault;

    public PaymentMethod() {
    }
}
