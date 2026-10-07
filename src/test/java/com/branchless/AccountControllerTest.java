package com.branchless;

import com.branchless.controller.AccountController;
import com.branchless.dto.MoneyRequest;
import com.branchless.dto.TransferRequest;
import com.branchless.entity.Account;
import com.branchless.entity.Customer;
import com.branchless.entity.Transaction;
import com.branchless.exception.AccountBlockedException;
import com.branchless.exception.AccountNotFoundException;
import com.branchless.exception.InsufficientBalanceException;
import com.branchless.service.AccountService;
import com.branchless.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAccount() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass", "1234567890");
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "ACTIVE", customer);

        when(accountService.createAccount(eq(1L), eq("SAVINGS"))).thenReturn(account);

        mockMvc.perform(post("/accounts?customerId=1&accountType=SAVINGS"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void testGetAccountsByCustomer() throws Exception {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass", "1234567890");
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "ACTIVE", customer);

        when(accountService.getCustomerAccounts(1L)).thenReturn(List.of(account));

        mockMvc.perform(get("/accounts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }

    @Test
    void testGetAccount() throws Exception {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.valueOf(500), "ACTIVE", null);

        when(accountService.getAccountByNumber("1234567890")).thenReturn(account);

        mockMvc.perform(get("/accounts/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    void testDepositSuccess() throws Exception {
        Transaction tx = new Transaction(1L, "DEPOSIT", BigDecimal.valueOf(200), null, "1234567890",
                LocalDateTime.now(), "Deposit");

        when(transactionService.deposit(eq("1234567890"), any(BigDecimal.class))).thenReturn(tx);

        MoneyRequest request = new MoneyRequest(BigDecimal.valueOf(200));

        mockMvc.perform(post("/accounts/1234567890/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void testWithdrawSuccess() throws Exception {
        Transaction tx = new Transaction(1L, "WITHDRAW", BigDecimal.valueOf(100), "1234567890", null,
                LocalDateTime.now(), "Withdrawal");

        when(transactionService.withdraw(eq("1234567890"), any(BigDecimal.class))).thenReturn(tx);

        MoneyRequest request = new MoneyRequest(BigDecimal.valueOf(100));

        mockMvc.perform(post("/accounts/1234567890/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAW"));
    }

    @Test
    void testWithdrawInsufficientBalance() throws Exception {
        when(transactionService.withdraw(eq("1234567890"), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        MoneyRequest request = new MoneyRequest(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/accounts/1234567890/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void testTransferSuccess() throws Exception {
        Transaction tx = new Transaction(1L, "TRANSFER", BigDecimal.valueOf(300), "1234567890", "9876543210",
                LocalDateTime.now(), "Transfer");

        when(transactionService.transfer(eq("1234567890"), eq("9876543210"), any(BigDecimal.class)))
                .thenReturn(tx);

        TransferRequest request = new TransferRequest("9876543210", BigDecimal.valueOf(300));

        mockMvc.perform(post("/accounts/1234567890/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.destinationAccount").value("9876543210"));
    }

    @Test
    void testTransferBlockedAccount() throws Exception {
        when(transactionService.transfer(eq("1234567890"), eq("9876543210"), any(BigDecimal.class)))
                .thenThrow(new AccountBlockedException("Account is blocked. Transfer not allowed."));

        TransferRequest request = new TransferRequest("9876543210", BigDecimal.valueOf(300));

        mockMvc.perform(post("/accounts/1234567890/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account is blocked. Transfer not allowed."));
    }
}
