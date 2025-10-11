package sg.nus.edu.shopping_cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sg.nus.edu.shopping_cart.service.CustomerService;
import sg.nus.edu.shopping_cart.model.Customer;
import sg.nus.edu.shopping_cart.utility.Result;
import sg.nus.edu.shopping_cart.utility.EmailUtil;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailUtil emailUtil;

    @GetMapping("/test")
    public String test() {
        return "CustomerController is working!";
    }

    @PostMapping("/")
    public Result<?> createCustomer(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String password = request.get("password");
            String email = request.get("email");

            if (name == null || password == null || email == null) {
                return Result.error("Name, password, and email are required");
            }

            Customer customer = new Customer();
            customer.setUsername(name);
            customer.setPassword(password);
            customer.setEmail(email);
            customerService.createCustomer(customer);
            return Result.success("Customer created successfully");

        } catch (Exception e) {
            return Result.error("Failed to create customer: " + e.getMessage());
        }
    }

    @PostMapping("customerlogin")
    public Result<Customer> customerlogin(@RequestBody Map<String, String> request) {
        System.out.println(request);
        String name = request.get("name");
        String password = request.get("password");

        Customer entity = customerService.getCustomerByName(name);

        if (entity == null) {
            System.out.println("Error: User not found in database");
            return Result.error("Invalid name or password");
        }

        System.out.println("In Database : [" + entity.getUsername() + "]");
        System.out.println("Database password: [" + entity.getPassword() + "]");
        System.out.println("Password match: " + entity.getPassword().equals(password));

        if (!entity.getPassword().equals(password)) {
            System.out.println("Error: Password does not match");
            return Result.error("Invalid name or password");
        }

        System.out.println("Login successful!");
        return Result.success(entity);
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
        Customer entity = customerService.getCustomerByName(name);
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

}
