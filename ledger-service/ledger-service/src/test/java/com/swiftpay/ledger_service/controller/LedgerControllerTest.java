package com.swiftpay.ledger_service.controller;

import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerTransaction;
import com.swiftpay.ledger_service.repositories.AccountRepository;
import com.swiftpay.ledger_service.repositories.LedgerTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link LedgerController}.
 */
class LedgerControllerTest {

    private MockMvc mockMvc;

    private LedgerTransactionRepository repository;
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(LedgerTransactionRepository.class);
        accountRepository = Mockito.mock(AccountRepository.class);

        LedgerController controller =
                new LedgerController(repository, accountRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldReturnTransactionHistory() throws Exception {

        LedgerTransaction txn1 = new LedgerTransaction();
        txn1.setTransactionId("TXN1001");

        LedgerTransaction txn2 = new LedgerTransaction();
        txn2.setTransactionId("TXN1002");

        when(repository.findBySenderIdOrReceiverId(1L, 1L))
                .thenReturn(List.of(txn1, txn2));

        mockMvc.perform(get("/v1/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("TXN1001"))
                .andExpect(jsonPath("$[1].transactionId").value("TXN1002"));
    }

    @Test
    void shouldReturnEmptyHistory() throws Exception {

        when(repository.findBySenderIdOrReceiverId(1L, 1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnBalanceWhenAccountExists() throws Exception {

        Account account = new Account();
        account.setUserId(1L);
        account.setBalance(new BigDecimal("1500.00"));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        mockMvc.perform(get("/v1/transactions/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.00"));
    }

    @Test
    void shouldReturnZeroWhenAccountNotFound() throws Exception {

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/transactions/accounts/99/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}