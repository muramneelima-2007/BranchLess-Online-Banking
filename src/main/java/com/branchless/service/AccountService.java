package com.branchless.service;

import com.branchless.entity.Account;
import com.branchless.entity.Customer;
import com.branchless.exception.AccountNotFoundException;
import com.branchless.exception.CustomerNotFoundException;
import com.branchless.repository.AccountRepository;
import com.branchless.repository.CustomerRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account createAccount(Long customerId, String accountType) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required to create an account");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        String resolvedType = (accountType != null && !accountType.trim().isEmpty())
                ? accountType.trim().toUpperCase()
                : "SAVINGS";

        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(resolvedType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public List<Account> getCustomerAccounts(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return accountRepository.findByCustomerId(customerId);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
    }

    public Account blockAccount(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        account.setStatus("BLOCKED");
        return accountRepository.save(account);
    }

    public Account activateAccount(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long) (ThreadLocalRandom.current().nextDouble() * 8999999999L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
