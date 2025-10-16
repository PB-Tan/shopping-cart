package sg.nus.edu.shopping_cart.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebAppConfig implements WebMvcConfigurer {
    @Autowired
    SecurityInterceptor securityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(securityInterceptor)
        .addPathPatterns(
            "/checkout/**", "/cart/**", "/order/**",
            // protect favourites endpoints (correct path is /favorites)
            "/favorites", "/favorites/**")
        .excludePathPatterns("/api/**", "/static/**", "/favicon.ico");
    }
}
