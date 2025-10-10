package sg.nus.edu.shopping_cart.validator;

import java.time.YearMonth;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import sg.nus.edu.shopping_cart.model.PaymentMethod;

@Component
public class PaymentMethodValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PaymentMethod.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PaymentMethod paymentMethod = (PaymentMethod) target;
        YearMonth now = YearMonth.now();
        Integer mm = paymentMethod.getExpiryMonth();
        Integer yy = paymentMethod.getExpiryYear();
        if (mm != null && yy != null) {
            int year = 2000 + yy;
            YearMonth submittedDate = YearMonth.of(year, mm);
            if (submittedDate.isBefore(now)) {
                errors.rejectValue("expiryYear", "errors.year.expiry", "Card has expired. Use another card.");
            }
        }
    }
}
