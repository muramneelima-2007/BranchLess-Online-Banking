package com.branchless.service;

import com.branchless.entity.Account;
import com.branchless.entity.Transaction;
import com.branchless.exception.AccountBlockedException;
import com.branchless.exception.AccountNotFoundException;
import com.branchless.exception.InsufficientBalanceException;
import com.branchless.exception.InvalidTransactionException;
import com.branchless.repository.AccountRepository;
import com.branchless.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AccountBlockedException("Account is blocked. Deposit not allowed.");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setSourceAccount("EXTERNAL_DEPOSIT");
        transaction.setDestinationAccount(accountNumber);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Deposit of " + amount + " to account " + accountNumber);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new AccountBlockedException("Account is blocked. Withdrawal not allowed.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Current balance: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setType("WITHDRAW");
        transaction.setAmount(amount);
        transaction.setSourceAccount(accountNumber);
        transaction.setDestinationAccount("CASH_WITHDRAWAL");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Withdrawal of " + amount + " from account " + accountNumber);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than zero");
        }

        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new InvalidTransactionException("Source and destination accounts cannot be the same");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Source account not found with account number: " + sourceAccountNumber));

        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found with account number: " + destinationAccountNumber));

        if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
            throw new AccountBlockedException("Source account is blocked. Transfer not allowed.");
        }

        if (!"ACTIVE".equalsIgnoreCase(destinationAccount.getStatus())) {
            throw new AccountBlockedException("Destination account is blocked. Transfer not allowed.");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source account. Current balance: " + sourceAccount.getBalance());
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = new Transaction();
        transaction.setType("TRANSFER");
        transaction.setAmount(amount);
        transaction.setSourceAccount(sourceAccountNumber);
        transaction.setDestinationAccount(destinationAccountNumber);
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Transfer of " + amount + " from account " + sourceAccountNumber + " to account " + destinationAccountNumber);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(String accountNumber) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountNotFoundException("Account not found with account number: " + accountNumber);
        }
        return transactionRepository.findBySourceAccountOrDestinationAccountOrderByDateDesc(accountNumber, accountNumber);
    }
}
