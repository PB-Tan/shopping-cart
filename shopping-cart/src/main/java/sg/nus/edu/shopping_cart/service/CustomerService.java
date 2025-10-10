package sg.nus.edu.shopping_cart.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.interfaces.*;
import sg.nus.edu.shopping_cart.model.*;
import sg.nus.edu.shopping_cart.repository.CustomerRepository;

@Service
@Transactional(readOnly = true)
public class CustomerService implements CustomerInterface {

    @Autowired
    CustomerRepository cr;

    public Optional<Customer> findCustomerByUsername(String username) {
        return cr.findById(username);
    }

    public Customer getCustomerByUsername(String username) {
        // implementation to package cookie in Cookie Auth Controller
        Customer customer = cr.findById(username).orElse(null);
        if (customer == null) {
            return null;
        }
        customer.setCart(null);
        customer.setOrders(null);
        customer.setPaymentMethods(null);
        return customer;
    }
}
