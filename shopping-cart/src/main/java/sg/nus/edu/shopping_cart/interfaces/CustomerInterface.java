package sg.nus.edu.shopping_cart.interfaces;

import java.util.Optional;

import sg.nus.edu.shopping_cart.model.*;

public interface CustomerInterface {
    public Optional<Customer> findCustomerByUsername(String username);

    public Customer getCustomerByUsername(String username);
}
