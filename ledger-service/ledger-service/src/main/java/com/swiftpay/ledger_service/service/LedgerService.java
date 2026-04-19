package com.swiftpay.ledger_service.service;

import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerTransaction;
import com.swiftpay.ledger_service.model.PaymentCompletedEvent;
import com.swiftpay.ledger_service.model.PaymentFailedEvent;
import com.swiftpay.ledger_service.model.PaymentInitiatedEvent;
import com.swiftpay.ledger_service.repositories.AccountRepository;
import com.swiftpay.ledger_service.repositories.LedgerTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void processTransfer(String message) {
        PaymentInitiatedEvent event = objectMapper.readValue(message, PaymentInitiatedEvent.class);
        Account sender =
                accountRepository.findById(event.getSenderId())
                        .orElseThrow();

        Optional<Account> receiverOptional =
                accountRepository.findById(event.getReceiverId());
        if (receiverOptional.isEmpty()) {

            kafkaTemplate.send(
                    "payment-failed",
                    objectMapper.writeValueAsString(
                            new PaymentFailedEvent(
                                    event.getTransactionId(),
                                    "RECEIVER NOT FOUND"
                            ))
            );
            return;
        }

        // Debit
        sender.setBalance(sender.getBalance().subtract(event.getAmount()));

        Account receiver = receiverOptional.get();

        receiver.setBalance(
                receiver.getBalance().

                        add(event.getAmount())
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Save transaction
        LedgerTransaction tx = LedgerTransaction.builder()
                .transactionId(event.getTransactionId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);


        kafkaTemplate.send(
                "payment-completed",
                objectMapper.writeValueAsString(
                        new

                                PaymentCompletedEvent(
                                event.getTransactionId(),
                                "SUCCESS",
                                "Transferred successfully"
                        ))
        );
    }
}