package com.swiftpay.ledger_service.controller;

import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerTransaction;
import com.swiftpay.ledger_service.repositories.AccountRepository;
import com.swiftpay.ledger_service.repositories.LedgerTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Ledger APIs", description = "Transaction history and balance APIs")
public class LedgerController {

    private final LedgerTransactionRepository repository;
    private final AccountRepository accountRepository;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get transaction history",
            description = "Returns all sent and received transactions for a user"
    )
    public List<LedgerTransaction> history(@PathVariable Long userId) {

        return repository.findBySenderIdOrReceiverId(userId, userId);
    }

    @GetMapping("/accounts/{userId}/balance")
    @Operation(
            summary = "Get account balance",
            description = "Returns current balance for the given user"
    )
    public BigDecimal balance(@PathVariable Long userId) {

        return accountRepository.findById(userId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}