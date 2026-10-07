package com.branchless;

import com.branchless.controller.AdminController;
import com.branchless.entity.Account;
import com.branchless.entity.Customer;
import com.branchless.service.AccountService;
import com.branchless.service.CustomerService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AccountService accountService;

    @Test
    void testGetAllCustomers() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass", "1234567890");
        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void testGetAllAccounts() throws Exception {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "ACTIVE", null);
        when(accountService.getAllAccounts()).thenReturn(List.of(account));

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }

    @Test
    void testBlockAccount() throws Exception {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "BLOCKED", null);
        when(accountService.blockAccount("1234567890")).thenReturn(account);

        mockMvc.perform(put("/admin/accounts/1234567890/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void testActivateAccount() throws Exception {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "ACTIVE", null);
        when(accountService.activateAccount("1234567890")).thenReturn(account);

        mockMvc.perform(put("/admin/accounts/1234567890/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
