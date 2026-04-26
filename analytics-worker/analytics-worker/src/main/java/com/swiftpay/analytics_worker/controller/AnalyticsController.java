package com.swiftpay.analytics_worker.controller;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import com.swiftpay.analytics_worker.repository.AnalyticsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs")
public class AnalyticsController {

    private final AnalyticsRepository repository;

    @GetMapping
    @Operation(
            summary = "Get All Analytics Records",
            description = "Fetch all analytics records from database"
    )
    @ApiResponse(responseCode = "200", description = "Records fetched successfully")
    public List<AnalyticsRecord> getAll() {
        return repository.findAll();
    }

    @GetMapping("/count")
    @Operation(
            summary = "Get Analytics Count",
            description = "Returns total number of analytics records"
    )
    @ApiResponse(responseCode = "200", description = "Count fetched successfully")
    public long count() {
        return repository.count();
    }
}