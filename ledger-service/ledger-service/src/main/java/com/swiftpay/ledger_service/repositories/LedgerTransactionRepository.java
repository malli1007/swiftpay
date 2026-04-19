package com.swiftpay.ledger_service.repositories;

import com.swiftpay.ledger_service.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerTransactionRepository
        extends JpaRepository<LedgerTransaction, Long> {

    List<LedgerTransaction> findBySenderIdOrReceiverId(
            Long senderId,
            Long receiverId
    );
}
