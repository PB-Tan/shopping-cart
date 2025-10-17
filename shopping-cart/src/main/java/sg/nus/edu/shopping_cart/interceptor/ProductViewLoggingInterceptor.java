package sg.nus.edu.shopping_cart.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.model.Product;
import sg.nus.edu.shopping_cart.model.ProductViewLog;
import sg.nus.edu.shopping_cart.service.ProductViewLogService;

@Component
public class ProductViewLoggingInterceptor implements HandlerInterceptor {
    @Autowired
    private ProductViewLogService productViewLogService;

    @Override
    public void postHandle(
            HttpServletRequest request, 
            HttpServletResponse response,
            Object handler, 
            ModelAndView modelAndView) {
        if (modelAndView == null || request.getRequestURI() == null) {
            return;
        }
        if (!"GET".equals(request.getMethod())) {
            return;
        }
        String uri = request.getRequestURI();
        if (!uri.matches("/catalogue/\\d+")) {     // only detail pages
            return;
        }

        Object productAttr = modelAndView.getModel().get("productlist");
        if (!(productAttr instanceof Product product)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String username = session != null ? (String) session.getAttribute("username") : null;

        ProductViewLog log = new ProductViewLog();
        log.setUsername(username);
        log.setProductId(product.getId());
        log.setProductName(product.getName());
        productViewLogService.save(log);
    }
}
