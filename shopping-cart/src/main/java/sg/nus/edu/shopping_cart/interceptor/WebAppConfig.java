package sg.nus.edu.shopping_cart.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebAppConfig implements WebMvcConfigurer {
    @Autowired
    SecurityInterceptor securityInterceptor;

    @Autowired
    SearchLoggingInterceptor searchLoggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(securityInterceptor)
        .addPathPatterns(
            "/checkout/**", "/cart/**", "/order/**",
            // protect favourites endpoints (correct path is /favorites)
            "/favorites", "/favorites/**")
        .excludePathPatterns("/api/**", "/static/**", "/favicon.ico");

        registry.addInterceptor(searchLoggingInterceptor)
                .addPathPatterns("/catalogue/search");
    }
}
