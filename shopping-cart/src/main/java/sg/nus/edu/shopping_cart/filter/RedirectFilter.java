// package sg.nus.edu.shopping_cart.filter;

// import jakarta.servlet.*;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.Ordered;
// import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Component;

// import java.io.IOException;

// @Component
// @Order(Ordered.HIGHEST_PRECEDENCE)
// public class RedirectFilter implements Filter {

//     @Value("${frontend.login.url:https://localhost:5173/login}")
//     private String frontendLoginUrl;

//     @Override
//     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//         HttpServletRequest req = (HttpServletRequest) request;
//         HttpServletResponse res = (HttpServletResponse) response;

//         String path = req.getRequestURI();

        
//         if (path.equals("/favorites") || path.equals("/favorites/")) {
//             HttpSession session = req.getSession(false);
//             boolean loggedIn = session != null && session.getAttribute("username") != null;
//             if (!loggedIn) {
//                 res.sendRedirect(frontendLoginUrl);
//                 return;
//             }
//         }

//         chain.doFilter(request, response);
//     }
// }
