package sg.nus.edu.shopping_cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.nus.edu.shopping_cart.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    // derived query
    public Optional<Shipment> findByOrderId(int id);
}
