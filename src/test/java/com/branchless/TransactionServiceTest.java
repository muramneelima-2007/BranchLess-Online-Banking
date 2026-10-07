package com.branchless;

import com.branchless.entity.Account;
import com.branchless.entity.Transaction;
import com.branchless.exception.AccountBlockedException;
import com.branchless.exception.InsufficientBalanceException;
import com.branchless.exception.InvalidTransactionException;
import com.branchless.repository.AccountRepository;
import com.branchless.repository.TransactionRepository;
import com.branchless.service.TransactionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(accountRepository, transactionRepository);
    }

    @Test
    void testDepositSuccess() {
        Account account = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(500), "ACTIVE", null);
        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = transactionService.deposit("1001", BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(700), account.getBalance());
        assertEquals("DEPOSIT", tx.getType());
        assertEquals(BigDecimal.valueOf(200), tx.getAmount());
        assertEquals("1001", tx.getDestinationAccount());
    }

    @Test
    void testDepositBlockedAccount() {
        Account account = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(500), "BLOCKED", null);
        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(account));

        assertThrows(AccountBlockedException.class, () -> transactionService.deposit("1001", BigDecimal.valueOf(200)));
    }

    @Test
    void testDepositInvalidAmount() {
        assertThrows(InvalidTransactionException.class, () -> transactionService.deposit("1001", BigDecimal.ZERO));
        assertThrows(InvalidTransactionException.class, () -> transactionService.deposit("1001", BigDecimal.valueOf(-50)));
    }

    @Test
    void testWithdrawSuccess() {
        Account account = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(500), "ACTIVE", null);
        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = transactionService.withdraw("1001", BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(300), account.getBalance());
        assertEquals("WITHDRAW", tx.getType());
        assertEquals(BigDecimal.valueOf(200), tx.getAmount());
        assertEquals("1001", tx.getSourceAccount());
    }

    @Test
    void testWithdrawInsufficientBalance() {
        Account account = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(100), "ACTIVE", null);
        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.withdraw("1001", BigDecimal.valueOf(500)));
    }

    @Test
    void testTransferSuccess() {
        Account source = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(1000), "ACTIVE", null);
        Account dest = new Account(2L, "1002", "SAVINGS", BigDecimal.valueOf(200), "ACTIVE", null);

        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("1002")).thenReturn(Optional.of(dest));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = transactionService.transfer("1001", "1002", BigDecimal.valueOf(300));

        assertEquals(BigDecimal.valueOf(700), source.getBalance());
        assertEquals(BigDecimal.valueOf(500), dest.getBalance());
        assertEquals("TRANSFER", tx.getType());
        assertEquals("1001", tx.getSourceAccount());
        assertEquals("1002", tx.getDestinationAccount());
    }

    @Test
    void testTransferSameAccount() {
        assertThrows(InvalidTransactionException.class, () ->
                transactionService.transfer("1001", "1001", BigDecimal.valueOf(100)));
    }

    @Test
    void testTransferSourceBlocked() {
        Account source = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(1000), "BLOCKED", null);
        Account dest = new Account(2L, "1002", "SAVINGS", BigDecimal.valueOf(200), "ACTIVE", null);

        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("1002")).thenReturn(Optional.of(dest));

        assertThrows(AccountBlockedException.class, () ->
                transactionService.transfer("1001", "1002", BigDecimal.valueOf(100)));
    }

    @Test
    void testTransferDestinationBlocked() {
        Account source = new Account(1L, "1001", "SAVINGS", BigDecimal.valueOf(1000), "ACTIVE", null);
        Account dest = new Account(2L, "1002", "SAVINGS", BigDecimal.valueOf(200), "BLOCKED", null);

        when(accountRepository.findByAccountNumber("1001")).thenReturn(Optional.of(source));
        when(accountRepository.findByAccountNumber("1002")).thenReturn(Optional.of(dest));

        assertThrows(AccountBlockedException.class, () ->
                transactionService.transfer("1001", "1002", BigDecimal.valueOf(100)));
    }

    @Test
    void testGetAccountTransactions() {
        when(accountRepository.existsByAccountNumber("1001")).thenReturn(true);
        when(transactionRepository.findBySourceAccountOrDestinationAccountOrderByDateDesc("1001", "1001"))
                .thenReturn(List.of(new Transaction()));

        List<Transaction> transactions = transactionService.getAccountTransactions("1001");
        assertEquals(1, transactions.size());
    }
}
