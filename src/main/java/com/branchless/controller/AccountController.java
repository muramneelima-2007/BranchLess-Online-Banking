package com.branchless.controller;

import com.branchless.dto.MoneyRequest;
import com.branchless.dto.TransferRequest;
import com.branchless.entity.Account;
import com.branchless.entity.Transaction;
import com.branchless.service.AccountService;
import com.branchless.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false, defaultValue = "SAVINGS") String accountType,
            @RequestBody(required = false) Map<String, Object> body) {
        Long resolvedCustomerId = customerId;
        String resolvedAccountType = accountType;

        if (body != null) {
            if (body.get("customerId") != null) {
                resolvedCustomerId = Long.valueOf(body.get("customerId").toString());
            }
            if (body.get("accountType") != null) {
                resolvedAccountType = body.get("accountType").toString();
            }
        }

        Account account = accountService.createAccount(resolvedCustomerId, resolvedAccountType);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomer(@PathVariable Long customerId) {
        List<Account> accounts = accountService.getCustomerAccounts(customerId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Transaction> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody MoneyRequest request) {
        Transaction transaction = transactionService.deposit(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody MoneyRequest request) {
        Transaction transaction = transactionService.withdraw(accountNumber, request.getAmount());
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<Transaction> transfer(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transfer(
                accountNumber,
                request.getDestinationAccount(),
                request.getAmount()
        );
        return ResponseEntity.ok(transaction);
    }
}
