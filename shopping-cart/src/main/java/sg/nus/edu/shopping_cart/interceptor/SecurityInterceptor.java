package sg.nus.edu.shopping_cart.interceptor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.*;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {
        LOGGER.info("LoggingInterceptor preHandle");
        LOGGER.info("Request Session: {}",
                request.getSession().getAttribute("username"));
        System.out.println("inside preHandle");
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            response.sendRedirect("/login");
            System.out.println("kicked out");
            return false;
        }

        return true;
    }
}
