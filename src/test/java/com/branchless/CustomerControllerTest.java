package com.branchless;

import com.branchless.controller.CustomerController;
import com.branchless.dto.LoginRequest;
import com.branchless.entity.Customer;
import com.branchless.exception.CustomerNotFoundException;
import com.branchless.exception.InvalidCredentialsException;
import com.branchless.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterCustomer() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass123", "9876543210");
        when(customerService.register(any(Customer.class))).thenReturn(customer);

        String jsonPayload = """
                {
                    "name": "Alice",
                    "email": "alice@example.com",
                    "password": "pass123",
                    "phone": "9876543210"
                }
                """;

        mockMvc.perform(post("/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testRegisterValidationFailure() throws Exception {
        Customer invalidCustomer = new Customer(null, "", "invalid-email", "", "");

        mockMvc.perform(post("/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLoginSuccess() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass123", "9876543210");
        LoginRequest request = new LoginRequest("alice@example.com", "pass123");

        when(customerService.login(any(LoginRequest.class))).thenReturn(customer);

        mockMvc.perform(post("/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("alice@example.com", "wrongpass");
        when(customerService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testGetProfileFound() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass123", "9876543210");
        when(customerService.getProfile(1L)).thenReturn(customer);

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void testGetProfileNotFound() throws Exception {
        when(customerService.getProfile(99L))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(get("/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id: 99"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        Customer updated = new Customer(1L, "Alice Smith", "alice@example.com", "pass123", "9876543210");
        when(customerService.updateProfile(eq(1L), any(Customer.class))).thenReturn(updated);

        Customer updateRequest = new Customer(null, "Alice Smith", null, null, "9876543210");

        mockMvc.perform(put("/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }
}
