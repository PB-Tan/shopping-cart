package sg.nus.edu.shopping_cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sg.nus.edu.shopping_cart.service.CustomerService;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.utility.Result;
import sg.nus.edu.shopping_cart.utility.EmailUtil;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@CrossOrigin
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailUtil emailUtil;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${google.client.secret:}")
    private String googleClientSecret;
    @Value("${google.redirect.uri:}")
    private String googleRedirectUri;
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/test")
    public String test() {
        return "CustomerController is working!";
    }

    // Start Google OAuth2 authorization (redirect user to Google's consent screen)
    @GetMapping("/oauth2/authorize/google")
    public void googleAuthorize(HttpServletResponse response) {
        try {
            String redirectUri = googleRedirectUri;
            if (redirectUri == null || redirectUri.isEmpty()) {
                redirectUri = "http://localhost:8080/api/customers/google/callback";
            }
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                    + "&response_type=code&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                    + "&access_type=offline&prompt=consent";
            response.sendRedirect(authUrl);
                } catch (Exception e) {
            try { response.sendRedirect("https://localhost:5173/login"); } catch (Exception ex) {}
        }
    }

    // OAuth2 callback: exchange code for tokens, validate id_token, create/find user and set session
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(name = "code", required = false) String code,
                               HttpSession session, HttpServletResponse response) {
        try {
            if (code == null || code.isEmpty()) {
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            String tokenUrl = "https://oauth2.googleapis.com/token";
            String redirectUri = "http://localhost:8080/api/customers/google/callback";

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
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            Map<String, Object> tokenBody = mapper.readValue(tokenResp.body(), Map.class);
            String idToken = (String) tokenBody.get("id_token");
            if (idToken == null) {
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            // Validate id_token by calling Google's tokeninfo endpoint
            String infoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest infoReq = HttpRequest.newBuilder().uri(URI.create(infoUrl)).GET().build();
            HttpResponse<String> infoResp = client.send(infoReq, HttpResponse.BodyHandlers.ofString());
            if (infoResp.statusCode() != 200) {
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            Map<String, Object> info = mapper.readValue(infoResp.body(), Map.class);
            // verify audience
            if (!googleClientId.equals(info.get("aud"))) {
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            String email = (String) info.get("email");
            String sub = (String) info.get("sub");
            String name = (String) info.get("name");

            if (email == null || sub == null) {
                response.sendRedirect("https://localhost:5173/login");
                return;
            }

            // find user by provider id or email
            Customer entity = null;
            // try providerCustomerId first
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
                // ensure username uniqueness
                int suffix = 1;
                String base = username;
                while (customerService.getCustomerByName(username) != null) {
                    username = base + suffix;
                    suffix++;
                }
                Customer newCust = new Customer();
                newCust.setUsername(username);
                // create placeholder password (not used)
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

            // write username into session and redirect to front-end profile
            session.setAttribute("username", entity.getUsername());
                response.sendRedirect("https://localhost:5173/profile");
        } catch (Exception e) {
            try { response.sendRedirect("https://localhost:5173/login"); } catch (Exception ex) {}
        }
    }

    // Accept id_token from Google Platform JS (front-end) and validate it server-side
    @PostMapping("/google/token")
    public Result<Customer> googleToken(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String idToken = body.get("id_token");
            if (idToken == null || idToken.isEmpty()) {
                return Result.error("Missing id_token");
            }

            // Validate id_token via Google tokeninfo endpoint
            String infoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest infoReq = HttpRequest.newBuilder().uri(URI.create(infoUrl)).GET().build();
            HttpResponse<String> infoResp = client.send(infoReq, HttpResponse.BodyHandlers.ofString());
            if (infoResp.statusCode() != 200) {
                return Result.error("Invalid id_token");
            }

            Map<String, Object> info = mapper.readValue(infoResp.body(), Map.class);
            if (!googleClientId.equals(info.get("aud"))) {
                return Result.error("Invalid audience");
            }

            String email = (String) info.get("email");
            String sub = (String) info.get("sub");
            String name = (String) info.get("name");
            if (email == null || sub == null) {
                return Result.error("Invalid token payload");
            }

            Customer entity = null;
            java.util.List<Customer> byProvider = customerService.findByProviderCustomerId(sub);
            if (byProvider != null && !byProvider.isEmpty()) {
                entity = byProvider.get(0);
            }
            if (entity == null) {
                entity = customerService.getCustomerByEmail(email);
            }

            if (entity == null) {
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

            session.setAttribute("username", entity.getUsername());
            return Result.success(entity);
        } catch (Exception e) {
            return Result.error("Failed to validate id_token: " + e.getMessage());
        }
    }

    @PostMapping("/")
    public Result<?> createCustomer(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            // support client-side hashed password
            String passwordHash = request.get("passwordHash");
            String passwordSalt = request.get("passwordSalt");
            String email = request.get("email");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String phoneNumber = request.get("phoneNumber");
            String address = request.get("address");
            String country = request.get("country");
            String postalCode = request.get("postalCode");

            if (name == null || password == null || email == null || firstName == null
                    || lastName == null || phoneNumber == null || address == null
                    || country == null || postalCode == null) {
                return Result.error("All customer fields are required");
            }

            // 检查用户名是否已存在，防止重复创建
            Customer existing = customerService.getCustomerByName(name);
            if (existing != null) {
                return Result.error("Username already exists");
            }

            Customer customer = new Customer();
            customer.setUsername(name);
            // store incoming hash/salt if provided, otherwise store raw password (legacy)
            if (passwordHash != null && !passwordHash.isEmpty() && passwordSalt != null && !passwordSalt.isEmpty()) {
                customer.setPassword(passwordHash);
                customer.setPasswordSalt(passwordSalt);
            } else if (request.containsKey("password")) {
                customer.setPassword(request.get("password"));
            } else {
                return Result.error("Password is required");
            }

            customer.setEmail(email);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPhoneNumber(phoneNumber);
            customer.setAddress(address);
            customer.setCountry(country);
            customer.setPostalCode(postalCode);
            customerService.createCustomer(customer);
            return Result.success("Customer created successfully");

        } catch (Exception e) {
            return Result.error("Failed to create customer: " + e.getMessage());
        }
    }

    @PostMapping("customerlogin")
    public Result<Customer> customerlogin(@RequestBody Map<String, String> request, HttpSession session) {
        System.out.println(request);
        String name = request.get("name");
        String password = request.get("password");
        String passwordHash = request.get("passwordHash");
        String passwordSalt = request.get("passwordSalt");

        Customer entity = customerService.getCustomerByName(name);

        if (entity == null) {
            System.out.println("Error: User not found in database");
            return Result.error("Invalid name or password");
        }

        System.out.println("In Database : [" + entity.getUsername() + "]");

        // If client sent passwordHash + salt, compare directly to stored hash
        if (passwordHash != null && passwordSalt != null) {
            String dbHash = entity.getPassword();
            if (dbHash == null || !dbHash.equals(passwordHash)) {
                System.out.println("Error: Password hash does not match");
                return Result.error("Invalid name or password");
            }
        } else if (password != null) {
            // Legacy path: compare raw password stored in DB (insecure) or computed hash fallback
            String dbHash = entity.getPassword();
            String dbSalt = entity.getPasswordSalt();
            if (dbSalt != null && !dbSalt.isEmpty()) {
                // compute SHA-256(salt + password) server-side and compare
                try {
                    java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                    byte[] digest = md.digest((dbSalt + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) sb.append(String.format("%02x", b));
                    String computed = sb.toString();
                    if (!computed.equals(dbHash)) {
                        System.out.println("Error: computed hash does not match");
                        return Result.error("Invalid name or password");
                    }
                } catch (Exception ex) {
                    return Result.error("Server error computing hash");
                }
            } else {
                // fallback to direct compare
                if (!entity.getPassword().equals(password)) {
                    System.out.println("Error: Password does not match");
                    return Result.error("Invalid name or password");
                }
            }
        } else {
            return Result.error("Password required");
        }

        System.out.println("Login successful!");
        // 将用户名写入 session（用于后续基于 session 的登录检测）
        try {
            session.setAttribute("username", name);
        } catch (Exception e) {
            System.out.println("Warning: failed to set session attribute: " + e.getMessage());
        }

        return Result.success(entity);
    }

    // 返回当前会话中的用户（如果已登录）
    @GetMapping("/session")
    public Result<Customer> getSessionUser(HttpSession session) {
        String name = (String) session.getAttribute("username");
        if (name == null || name.isEmpty()) {
            return Result.error("Not logged in");
        }
        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("User not found");
        }
        return Result.success(entity);
    }

    // Return salt for a given username so client can compute salted hash
    @GetMapping("/salt/{name}")
    public Result<Map<String, String>> getSalt(@PathVariable String name) {
        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) return Result.error("User not found");
        String salt = entity.getPasswordSalt();
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("salt", salt == null ? "" : salt);
        return Result.success(map);
    }

    // 登出：使 session 失效
    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        try {
            session.invalidate();
        } catch (Exception e) {
            // ignore
        }
        return Result.success("Logged out");
    }

    @PostMapping("customersendemail")
    public String postMethodName(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        Customer entity = customerService.getCustomerByName(name);
        if (entity != null) {
            String email = entity.getEmail();
            String code = emailUtil.generateVerificationCode();
            System.out.println(code);
            emailUtil.sendVerificationCode(email, code);
            return code;
        }
        return "User not found";
    }

    @PostMapping("customerchangepassword")
    public Result<?> customerchangepassword(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String newPassword = request.get("newPassword");

        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("Invalid name");
        }

        entity.setPassword(newPassword);
        customerService.updateCustomerPassworde(name, newPassword);
        return Result.success("Password changed successfully");
    }

    // 根据用户名获取用户信息
    @GetMapping("/name/{name}")
    public Result<Customer> getCustomerByName(@PathVariable String name) {
        Customer entity = customerService.getCustomerByUsername(name);
        if (entity == null) {
            return Result.error("User not found");
        }
        return Result.success(entity);
    }

    // 更新地址
    @PostMapping("/updateaddress")
    public Result<?> updateAddress(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String address = request.get("address");

        if (name == null || address == null) {
            return Result.error("Name and address are required");
        }

        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("User not found");
        }

        entity.setAddress(address);
        customerService.updateCustomer(entity);
        return Result.success("Address updated successfully");
    }

    // 更新电话
    @PostMapping("/updatephone")
    public Result<?> updatePhone(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String phone = request.get("phone");

        if (name == null || phone == null) {
            return Result.error("Name and phone are required");
        }

        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("User not found");
        }

        entity.setPhoneNumber(phone);
        customerService.updateCustomer(entity);
        return Result.success("Phone updated successfully");
    }

    // 更新邮箱
    @PostMapping("/updateemail")
    public Result<?> updateEmail(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");

        if (name == null || email == null) {
            return Result.error("Name and email are required");
        }

        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("User not found");
        }

        entity.setEmail(email);
        customerService.updateCustomer(entity);
        return Result.success("Email updated successfully");
    }

    // 更新用户个人资料（firstName, lastName, country等）
    @PostMapping("/updateprofile")
    public Result<?> updateProfile(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null) {
            return Result.error("Name is required");
        }

        Customer entity = customerService.getCustomerByName(name);
        if (entity == null) {
            return Result.error("User not found");
        }

        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String country = request.get("country");

        if (firstName != null) entity.setFirstName(firstName);
        if (lastName != null) entity.setLastName(lastName);
        if (country != null) entity.setCountry(country);

        customerService.updateCustomer(entity);
        return Result.success("Profile updated successfully");
    }

    // 向指定邮箱发送验证码
    @PostMapping("/sendemailto")
    public String sendEmailTo(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "Email is required";
        }

        String code = emailUtil.generateVerificationCode();
        System.out.println("Verification code for " + email + ": " + code);
        emailUtil.sendVerificationCode(email, code);
        return code;
    }

    // 检查用户名是否已存在（英文提示）
    @GetMapping("/exists/{name}")
    public Result<?> checkUsernameExists(@PathVariable String name) {
        if (name == null || name.isEmpty()) {
            return Result.error("Name is required");
        }

        Customer entity = customerService.getCustomerByName(name);
        if (entity != null) {
            return Result.error("Username already exists");
        }
        return Result.success("Username available", null);
    }

}
