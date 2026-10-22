package com.practice.bank.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApiTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void noCredentialsReturns401() throws Exception {
        mockMvc.perform(get("/api/me/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPinReturns401() throws Exception {
        mockMvc.perform(get("/api/me/account").with(httpBasic("1001", "9999")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnCustomerEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/me/account").with(httpBasic("admin", "admin123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerOnAdminEndpointReturns403() throws Exception {
        mockMvc.perform(get("/api/accounts").with(httpBasic("1001", "1234")))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerReadsOwnAccountWithoutSecrets() throws Exception {
        mockMvc.perform(get("/api/me/account").with(httpBasic("1001", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(1001))
                .andExpect(jsonPath("$.holder").value("George"))
                .andExpect(jsonPath("$.pinHash").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist());
    }

    @Test
    void depositReturnsUpdatedAccount() throws Exception {
        mockMvc.perform(post("/api/me/deposits")
                .with(httpBasic("1001", "1234"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": \"50.00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(1001));
    }

    @Test
    void insufficientFundsReturns422WithShortfall() throws Exception {
        mockMvc.perform(post("/api/me/withdrawals")
                .with(httpBasic("1001", "1234"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": \"99999\"}"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title").value("Insufficient funds"))
                .andExpect(jsonPath("$.shortfall").exists());
    }

    @Test
    void transferToUnknownAccountReturns404() throws Exception {
        mockMvc.perform(post("/api/me/transfers")
                .with(httpBasic("1001", "1234"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetAccount\": 9999, \"amount\": \"10\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidAmountReturns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/me/deposits")
                .with(httpBasic("1001", "1234"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": \"-5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void adminOpensAccountReturns201WithLocation() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .with(httpBasic("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"type": "SAVINGS", "holder": "Eleni", "pin": "2468", "openingDeposit": "0"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/accounts/")))
                .andExpect(jsonPath("$.holder").value("Eleni"));
    }

    @Test
    void closingAccountWithMoneyReturns409() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/accounts/1001")
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isConflict());
    }
}
