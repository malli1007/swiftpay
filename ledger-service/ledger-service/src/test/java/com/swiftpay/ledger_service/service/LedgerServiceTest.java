package com.swiftpay.ledger_service.service;

import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerTransaction;
import com.swiftpay.ledger_service.repositories.AccountRepository;
import com.swiftpay.ledger_service.repositories.LedgerTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerTransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private LedgerService ledgerService;

    private String validMessage;

    @BeforeEach
    void setup() {
        validMessage = """
            {
              "transactionId":"TXN1001",
              "senderId":1,
              "receiverId":2,
              "amount":500,
              "currency":"INR"
            }
            """;
    }

    @Test
    void shouldProcessTransferSuccessfully() {

        Account sender = new Account(1L, new BigDecimal("1000"));
        Account receiver = new Account(2L, new BigDecimal("200"));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(receiver));

        ledgerService.processTransfer(validMessage);

        assertEquals(new BigDecimal("500"), sender.getBalance());
        assertEquals(new BigDecimal("700"), receiver.getBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(LedgerTransaction.class));
        verify(kafkaTemplate).send(eq("payment-completed"), anyString());
    }

    @Test
    void shouldSendFailedWhenReceiverNotFound() {

        Account sender = new Account(1L, new BigDecimal("1000"));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));
        when(accountRepository.findById(2L))
                .thenReturn(Optional.empty());

        ledgerService.processTransfer(validMessage);

        verify(kafkaTemplate)
                .send(eq("payment-failed"), anyString());

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSenderNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> ledgerService.processTransfer(validMessage));

        verify(kafkaTemplate, never())
                .send(eq("payment-completed"), anyString());
    }

    @Test
    void shouldThrowForInvalidJson() {

        String invalid = "invalid-json";

        assertThrows(Exception.class,
                () -> ledgerService.processTransfer(invalid));
    }

    @Test
    void shouldSaveTransactionWithSuccessStatus() {

        Account sender = new Account(1L, new BigDecimal("1000"));
        Account receiver = new Account(2L, new BigDecimal("200"));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(receiver));

        ArgumentCaptor<LedgerTransaction> captor =
                ArgumentCaptor.forClass(LedgerTransaction.class);

        ledgerService.processTransfer(validMessage);

        verify(transactionRepository).save(captor.capture());

        LedgerTransaction tx = captor.getValue();

        assertEquals("TXN1001", tx.getTransactionId());
        assertEquals("SUCCESS", tx.getStatus());
        assertEquals(new BigDecimal("500"), tx.getAmount());
    }
}