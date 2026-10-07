package com.branchless.controller;

import com.branchless.dto.LoginRequest;
import com.branchless.entity.Customer;
import com.branchless.service.CustomerService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Customer> register(@Valid @RequestBody Customer customer) {
        Customer createdCustomer = customerService.register(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Customer customer = customerService.login(loginRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("customerId", customer.getId());
        response.put("name", customer.getName());
        response.put("email", customer.getEmail());
        response.put("phone", customer.getPhone());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getProfile(@PathVariable Long id) {
        Customer customer = customerService.getProfile(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateProfile(@PathVariable Long id, @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateProfile(id, customer);
        return ResponseEntity.ok(updatedCustomer);
    }
}
