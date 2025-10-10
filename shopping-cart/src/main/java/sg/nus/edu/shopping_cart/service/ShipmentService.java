package sg.nus.edu.shopping_cart.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.model.Shipment;
import sg.nus.edu.shopping_cart.repository.ShipmentRepository;
import sg.nus.edu.shopping_cart.interfaces.*;

@Service
@Transactional(readOnly = true)
public class ShipmentService implements ShipmentInterface {

    @Autowired
    ShipmentRepository sr;

    public Optional<Shipment> findShipmentByOrderId(int id) {
        return sr.findByOrderId(id);
    }
}
