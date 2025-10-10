package sg.nus.edu.shopping_cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.nus.edu.shopping_cart.interfaces.*;

@Service
@Transactional(readOnly = true)
public class PaymentMethodService implements PaymentMethodInterface {

}
