package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table
@Getter
@Setter
public class Customer {

    @Id
    private String username;

    @OneToOne(mappedBy = "customer") // inverse
    private Cart cart;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    @OneToMany(mappedBy = "customer")
    private List<PaymentMethod> paymentMethods;

    // edit by serene
    @OneToMany(mappedBy = "customer")
    private List<Favorite> favorites;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String address;
    private String country;
    private String postalCode;
    private String password;
    private String providerCustomerId;

    public Customer() {
    }
}
