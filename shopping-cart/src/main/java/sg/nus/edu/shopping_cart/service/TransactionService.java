package sg.nus.edu.shopping_cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.interfaces.TransactionInterface;

@Service
@Transactional(readOnly = true)
public class TransactionService implements TransactionInterface {

}
