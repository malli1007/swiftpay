package com.swiftpay.ledger_service.controller;

import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerTransaction;
import com.swiftpay.ledger_service.repositories.AccountRepository;
import com.swiftpay.ledger_service.repositories.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerTransactionRepository repository;
    private final AccountRepository accountRepository;

    @GetMapping("/{userId}")
    public List<LedgerTransaction> history(
            @PathVariable Long userId) {

        return repository.findBySenderIdOrReceiverId(
                userId,
                userId
        );
    }

    @GetMapping("/accounts/{userId}/balance")
    public BigDecimal balance(@PathVariable Long userId) {

        return accountRepository.findById(userId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}