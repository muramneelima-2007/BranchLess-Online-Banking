package com.branchless.controller;

import com.branchless.entity.Account;
import com.branchless.entity.Customer;
import com.branchless.service.AccountService;
import com.branchless.service.CustomerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final AccountService accountService;

    public AdminController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/accounts/{accountNumber}/block")
    public ResponseEntity<Account> blockAccount(@PathVariable String accountNumber) {
        Account blockedAccount = accountService.blockAccount(accountNumber);
        return ResponseEntity.ok(blockedAccount);
    }

    @PutMapping("/accounts/{accountNumber}/activate")
    public ResponseEntity<Account> activateAccount(@PathVariable String accountNumber) {
        Account activatedAccount = accountService.activateAccount(accountNumber);
        return ResponseEntity.ok(activatedAccount);
    }
}
