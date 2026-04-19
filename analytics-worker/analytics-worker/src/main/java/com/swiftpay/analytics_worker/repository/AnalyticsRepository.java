package com.swiftpay.analytics_worker.repository;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository
        extends JpaRepository<AnalyticsRecord, Long> {
}
