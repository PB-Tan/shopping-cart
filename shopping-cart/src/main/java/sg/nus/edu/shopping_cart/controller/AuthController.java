package sg.nus.edu.shopping_cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sg.nus.edu.shopping_cart.service.CustomerService;
import sg.nus.edu.shopping_cart.model.Customer;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${google.client.secret:}")
    private String googleClientSecret;

    // Allow configuring the exact redirect URI that Google will call, e.g. https://your-domain/api/auth/callback
    @Value("${google.redirect.uri:}")
    private String googleRedirectUri;

    // Frontend URL to redirect users after successful login (defaults to vite dev server)
    @Value("${frontend.url:https://localhost:5173}")
    private String frontendUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/api/auth/callback")
    public void authCallback(@RequestParam(name = "code", required = false) String code,
                             HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (code == null || code.isEmpty()) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            String tokenUrl = "https://oauth2.googleapis.com/token";

            String redirectUri = googleRedirectUri;
            if (redirectUri == null || redirectUri.isEmpty()) {
                // Build from request as a fallback (may not match registered Google redirect URI)
                String scheme = request.getScheme();
                String host = request.getServerName();
                int port = request.getServerPort();
                String portPart = "";
                if (!((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))) {
                    portPart = ":" + port;
                }
                redirectUri = scheme + "://" + host + portPart + "/api/auth/callback";
            }

            String form = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(googleClientSecret, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> tokenResp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (tokenResp.statusCode() != 200) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            Map<String, Object> tokenBody = mapper.readValue(tokenResp.body(), Map.class);
            String idToken = (String) tokenBody.get("id_token");
            if (idToken == null) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            // Validate id_token via Google's tokeninfo endpoint
            String infoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest infoReq = HttpRequest.newBuilder().uri(URI.create(infoUrl)).GET().build();
            HttpResponse<String> infoResp = client.send(infoReq, HttpResponse.BodyHandlers.ofString());
            if (infoResp.statusCode() != 200) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            Map<String, Object> info = mapper.readValue(infoResp.body(), Map.class);
            // verify audience
            if (!googleClientId.equals(info.get("aud"))) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            String email = (String) info.get("email");
            String sub = (String) info.get("sub");
            String name = (String) info.get("name");

            if (email == null || sub == null) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }

            // find user by provider id or email
            Customer entity = null;
            java.util.List<Customer> byProvider = customerService.findByProviderCustomerId(sub);
            if (byProvider != null && !byProvider.isEmpty()) {
                entity = byProvider.get(0);
            }
            if (entity == null) {
                entity = customerService.getCustomerByEmail(email);
            }

            if (entity == null) {
                // create a new user record
                String username = email.split("@")[0];
                int suffix = 1;
                String base = username;
                while (customerService.getCustomerByName(username) != null) {
                    username = base + suffix;
                    suffix++;
                }
                Customer newCust = new Customer();
                newCust.setUsername(username);
                newCust.setPassword("");
                newCust.setPasswordSalt(null);
                newCust.setEmail(email);
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ", 2);
                    newCust.setFirstName(parts[0]);
                    newCust.setLastName(parts.length > 1 ? parts[1] : "");
                } else {
                    newCust.setFirstName(name != null ? name : username);
                }
                newCust.setProviderCustomerId(sub);
                customerService.createCustomer(newCust);
                entity = newCust;
            }

            // set session and redirect to frontend profile
            session.setAttribute("username", entity.getUsername());
            response.sendRedirect(frontendUrl + "/profile");
        } catch (Exception e) {
            try { response.sendRedirect(frontendUrl + "/login"); } catch (Exception ex) {}
        }
    }

}
