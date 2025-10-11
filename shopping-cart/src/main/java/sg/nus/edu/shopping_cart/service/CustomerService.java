package sg.nus.edu.shopping_cart.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.repository.CustomerDAO;
import sg.nus.edu.shopping_cart.interfaces.CustomerInterface;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.repository.CustomerRepository;

@Service
public class CustomerService implements CustomerInterface {

    @Autowired
    CustomerDAO customerDAO;

    @Transactional
    public Customer createCustomer(Customer customer) {
        return customerDAO.save(customer);
    }

    public Customer getCustomerByName(String name) {
        return customerDAO.findById(name).orElse(null);
    }

    @Transactional
    public int updateCustomerPassworde(String name, String password) {
        Customer customer = customerDAO.findById(name).orElse(null);
        if (customer != null) {
            customer.setPassword(password);
            customerDAO.save(customer);
            return 0;
        }
        return 1;
    }

    @Transactional
    public Customer updateCustomer(Customer customer) {
        return customerDAO.save(customer);
    }

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
