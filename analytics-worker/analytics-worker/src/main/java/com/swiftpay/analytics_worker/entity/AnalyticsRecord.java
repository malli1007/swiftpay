package com.swiftpay.analytics_worker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String status;
    private LocalDateTime processedAt;
}
