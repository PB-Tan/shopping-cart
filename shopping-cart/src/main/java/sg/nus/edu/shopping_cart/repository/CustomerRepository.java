package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sg.nus.edu.shopping_cart.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {

}
