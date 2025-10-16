package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    // @Min(24)
    // @Max(99)
    // private int expiryYear;

    // @Min(1)
    // @Max(12)
    // private int expiryMonth;
    // private String cardType;

    @NotBlank(message = "Name is required")
    private String cardHolderName;

    // @NotBlank(message = "Card number is required")
    // @Transient
    // @Pattern(regexp = "\\d{16}", message = "16-digit number is required")
    // private String cardNumber;
    // private String lastFourDigits;

    // @Getter(AccessLevel.NONE)
    // @Setter(AccessLevel.NONE)
    // @Column(name = "is_default")
    // private boolean isDefault;

    public PaymentMethod() {
    }

    // public boolean getIsDefault() {
    // return isDefault;
    // }

    // public void setIsDefault(boolean isDefault) {
    // this.isDefault = isDefault;
    // }

    // public void setLastFourDigits(String cardNumber) {
    // if (cardNumber != null && cardNumber.length() == 16) {
    // this.cardNumber = cardNumber;
    // this.lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
    // }
    // }

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private int id;

    // private Integer expiryMonth;

    // private Integer expiryYear;
    // @NotNull(message = "Card Number is required")
    // @Size(min = 16, max = 16)
    // @Pattern(regexp = "\\d{16}", message = "Card number must contain 16 digits")
    // // ensures that all 16 are digits (0-9)
    // private String cardNumber;
    // private String cardBrand;
    // @NotBlank(message = "Name is required")
    // @Size(min = 2, max = 32)
    // private String cardHolderName;
    // private Integer lastFourDigits;
    // private boolean isDefault;
}
