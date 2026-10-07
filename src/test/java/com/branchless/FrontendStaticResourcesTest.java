package com.branchless;

import com.branchless.service.AccountService;
import com.branchless.service.CustomerService;
import com.branchless.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class FrontendStaticResourcesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testRootUrlForwardsToIndexHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void testIndexHtmlDirectlyServed() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("BranchLess")))
                .andExpect(content().string(containsString("Online Banking System")))
                .andExpect(content().string(containsString("id=\"auth-section\"")))
                .andExpect(content().string(containsString("id=\"dashboard-section\"")))
                .andExpect(content().string(containsString("src=\"script.js\"")));
    }

    @Test
    void testStyleCssServed() throws Exception {
        mockMvc.perform(get("/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"))
                .andExpect(content().string(containsString("--primary: #1e40af;")));
    }

    @Test
    void testScriptJsServed() throws Exception {
        mockMvc.perform(get("/script.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"))
                .andExpect(content().string(containsString("BranchLess")))
                .andExpect(content().string(containsString("handleLogin")))
                .andExpect(content().string(containsString("handleRegister")))
                .andExpect(content().string(containsString("handleDeposit")))
                .andExpect(content().string(containsString("handleWithdraw")))
                .andExpect(content().string(containsString("handleTransfer")));
    }
}
