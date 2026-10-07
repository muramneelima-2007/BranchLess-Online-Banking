package com.branchless;

import com.branchless.dto.LoginRequest;
import com.branchless.entity.Customer;
import com.branchless.exception.CustomerNotFoundException;
import com.branchless.exception.EmailAlreadyExistsException;
import com.branchless.exception.InvalidCredentialsException;
import com.branchless.repository.CustomerRepository;
import com.branchless.service.CustomerService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private PasswordEncoder passwordEncoder;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerService = new CustomerService(customerRepository, passwordEncoder);
    }

    @Test
    void testRegisterCustomerSuccess() {
        Customer customer = new Customer(null, "John Doe", "john@example.com", "password123", "9876543210");

        when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Customer result = customerService.register(customer);

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertNotEquals("password123", result.getPassword());
        assertTrue(passwordEncoder.matches("password123", result.getPassword()));
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testRegisterCustomerDuplicateEmail() {
        Customer customer = new Customer(null, "John Doe", "john@example.com", "password123", "9876543210");

        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> customerService.register(customer));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testLoginSuccess() {
        String encoded = passwordEncoder.encode("secretPass");
        Customer customer = new Customer(1L, "Alice", "alice@example.com", encoded, "1234567890");

        when(customerRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(customer));

        LoginRequest request = new LoginRequest("alice@example.com", "secretPass");
        Customer loggedIn = customerService.login(request);

        assertNotNull(loggedIn);
        assertEquals("Alice", loggedIn.getName());
    }

    @Test
    void testLoginInvalidPassword() {
        String encoded = passwordEncoder.encode("secretPass");
        Customer customer = new Customer(1L, "Alice", "alice@example.com", encoded, "1234567890");

        when(customerRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(customer));

        LoginRequest request = new LoginRequest("alice@example.com", "wrongPass");
        assertThrows(InvalidCredentialsException.class, () -> customerService.login(request));
    }

    @Test
    void testLoginCustomerNotFound() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@example.com", "secretPass");
        assertThrows(InvalidCredentialsException.class, () -> customerService.login(request));
    }

    @Test
    void testGetProfileNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getProfile(99L));
    }
}
