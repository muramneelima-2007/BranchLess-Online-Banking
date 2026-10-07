package com.branchless;

import com.branchless.controller.TransactionController;
import com.branchless.entity.Transaction;
import com.branchless.service.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testGetTransactionsByAccount() throws Exception {
        Transaction tx = new Transaction(1L, "DEPOSIT", BigDecimal.valueOf(500), null, "1234567890",
                LocalDateTime.now(), "Deposit");

        when(transactionService.getAccountTransactions("1234567890")).thenReturn(List.of(tx));

        mockMvc.perform(get("/transactions/account/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(500))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }
}
