package sg.nus.edu.shopping_cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sg.nus.edu.shopping_cart.model.Customer;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface CustomerDAO extends JpaRepository<Customer, String> {

    @Query("SELECT c FROM Customer c WHERE c.email = ?1")
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.phoneNumber = ?1")
    Optional<Customer> findByPhone(String phone);

    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.phoneNumber = ?2, c.address = ?3, c.email = ?4 WHERE c.username = ?1")
    void updateCustomer(String username, String phone, String address, String email);

}
