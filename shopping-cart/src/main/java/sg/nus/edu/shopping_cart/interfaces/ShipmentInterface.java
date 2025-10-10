package sg.nus.edu.shopping_cart.interfaces;

import java.util.Optional;

import sg.nus.edu.shopping_cart.model.*;

public interface ShipmentInterface {

    public Optional<Shipment> findShipmentByOrderId(int id);
}
