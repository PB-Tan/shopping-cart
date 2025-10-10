package sg.nus.edu.shopping_cart.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String courierName;
    private String serviceLevel;
    private String shipmentCode;
    private LocalDateTime deliveryEstimate;
    private LocalDateTime createdAt;
    private String shipmentMethod;

    @OneToOne(mappedBy = "shipment")
    private Order order;
}
