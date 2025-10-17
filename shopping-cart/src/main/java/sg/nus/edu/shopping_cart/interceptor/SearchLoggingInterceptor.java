package sg.nus.edu.shopping_cart.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sg.nus.edu.shopping_cart.interfaces.CartInterface;
import sg.nus.edu.shopping_cart.model.CartItem;
import sg.nus.edu.shopping_cart.model.SearchLog;
import sg.nus.edu.shopping_cart.service.SearchLogService;

@Component
public class SearchLoggingInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchLoggingInterceptor.class);

    @Autowired
    private CartInterface cartInterface;

    @Autowired
    private SearchLogService searchLogService;

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           @Nullable ModelAndView modelAndView) {
        if (request.getRequestURI() == null) {
            return;
        }
        // this request handles only on catalogue page search now..
        if (!request.getRequestURI().startsWith("/catalogue/search")) {
            return;
        }

        String searchby = request.getParameter("searchby");
        String keyword = request.getParameter("keyword");
        

        HttpSession session = request.getSession(false);
        String username = session == null ? null : (String) session.getAttribute("username");

        try {
            String cartSnapshot;
            if (username != null) {
                List<CartItem> cartItems = cartInterface.getCartItemsByCustomer(username);
                List<String> itemSummaries = new ArrayList<>();
                for (CartItem item : cartItems) {
                    itemSummaries.add(String.format("%s x%d (id=%d)",
                            item.getProduct().getName(), item.getQuantity(), item.getProduct().getId()));
                }
                cartSnapshot = String.join(", ", itemSummaries);
                LOGGER.info("Search by user='{}', searchby='{}', keyword='{}', cartItems=[{}]",
                        username, searchby, keyword, cartSnapshot);
            } else {
                cartSnapshot = "guest user";
                LOGGER.info("Search by guest, searchby='{}', keyword='{}', cartItems=[{}]",
                        searchby, keyword, cartSnapshot);
            }

            SearchLog log = new SearchLog();
            log.setUsername(username);
            log.setSearchby(searchby);
            log.setKeyword(keyword);
            log.setCartSnapshot(cartSnapshot);
            // capture result count from model (if controller added 'products')
            if (modelAndView != null) {
                Object productsAttr = modelAndView.getModel().get("products");
                if (productsAttr instanceof java.util.List<?> list) {
                    log.setResultCount(list.size());
                }
            }
            searchLogService.save(log);
        } catch (Exception e) {
            LOGGER.warn("Failed to persist search log: {}", e.getMessage());
        }
    }// searchHistory 
}

