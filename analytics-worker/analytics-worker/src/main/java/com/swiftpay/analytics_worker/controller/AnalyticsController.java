package com.swiftpay.analytics_worker.controller;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import com.swiftpay.analytics_worker.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsRepository repository;

    @GetMapping
    public List<AnalyticsRecord> getAll() {
        return repository.findAll();
    }

    @GetMapping("/count")
    public long count() {
        return repository.count();
    }
}
