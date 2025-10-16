// package sg.nus.edu.shopping_cart.controller;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;

// @Controller
// @Deprecated
// // NOTE: This controller previously mapped /catalogue and caused an ambiguous mapping
// // with CatalogueController. Kept here for reference but mapping changed to avoid conflicts.
// public class RedirectController {

//     // configurable frontend login URL; default to Vite dev server
//     @Value("${frontend.login.url:https://localhost:5173/login}")
//     private String frontendLoginUrl;

//     // Disabled original /catalogue mapping to avoid conflict; kept for reference.
//     @GetMapping("/redirect-catalogue-disabled")
//     public String redirectCatalogueToFrontend() {
//         return "redirect:" + frontendLoginUrl;
//     }
// }
