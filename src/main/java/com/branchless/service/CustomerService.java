package com.branchless.service;

import com.branchless.dto.LoginRequest;
import com.branchless.entity.Customer;
import com.branchless.exception.CustomerNotFoundException;
import com.branchless.exception.EmailAlreadyExistsException;
import com.branchless.exception.InvalidCredentialsException;
import com.branchless.repository.CustomerRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + customer.getEmail());
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    public Customer login(LoginRequest loginRequest) {
        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return customer;
    }

    public Customer getProfile(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    public Customer updateProfile(Long id, Customer updatedData) {
        Customer existing = getProfile(id);

        if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
            existing.setName(updatedData.getName().trim());
        }
        if (updatedData.getPhone() != null && !updatedData.getPhone().trim().isEmpty()) {
            existing.setPhone(updatedData.getPhone().trim());
        }

        return customerRepository.save(existing);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
