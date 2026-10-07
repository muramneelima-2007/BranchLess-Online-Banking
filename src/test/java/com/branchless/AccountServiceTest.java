package com.branchless;

import com.branchless.entity.Account;
import com.branchless.entity.Customer;
import com.branchless.exception.AccountNotFoundException;
import com.branchless.exception.CustomerNotFoundException;
import com.branchless.repository.AccountRepository;
import com.branchless.repository.CustomerRepository;
import com.branchless.service.AccountService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, customerRepository);
    }

    @Test
    void testCreateAccountSuccess() {
        Customer customer = new Customer(1L, "Alice", "alice@example.com", "pass", "1234567890");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount(1L, "SAVINGS");

        assertNotNull(account);
        assertNotNull(account.getAccountNumber());
        assertEquals("SAVINGS", account.getAccountType());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("ACTIVE", account.getStatus());
        assertEquals(customer, account.getCustomer());
    }

    @Test
    void testCreateAccountCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> accountService.createAccount(99L, "SAVINGS"));
    }

    @Test
    void testBlockAccount() {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "ACTIVE", null);
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account blocked = accountService.blockAccount("1234567890");

        assertEquals("BLOCKED", blocked.getStatus());
    }

    @Test
    void testActivateAccount() {
        Account account = new Account(1L, "1234567890", "SAVINGS", BigDecimal.ZERO, "BLOCKED", null);
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account activated = accountService.activateAccount("1234567890");

        assertEquals("ACTIVE", activated.getStatus());
    }

    @Test
    void testGetAccountByNumberNotFound() {
        when(accountRepository.findByAccountNumber("9999999999")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByNumber("9999999999"));
    }
}
