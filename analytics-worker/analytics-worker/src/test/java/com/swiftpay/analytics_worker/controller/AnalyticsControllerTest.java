package com.swiftpay.analytics_worker.controller;

import com.swiftpay.analytics_worker.entity.AnalyticsRecord;
import com.swiftpay.analytics_worker.repository.AnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AnalyticsController}.
 */
class AnalyticsControllerTest {

    private MockMvc mockMvc;
    private AnalyticsRepository repository;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(AnalyticsRepository.class);

        AnalyticsController controller =
                new AnalyticsController(repository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldReturnAllAnalyticsRecords() throws Exception {

        AnalyticsRecord record1 = new AnalyticsRecord();
        record1.setTransactionId("TXN1001");

        AnalyticsRecord record2 = new AnalyticsRecord();
        record2.setTransactionId("TXN1002");

        when(repository.findAll())
                .thenReturn(List.of(record1, record2));

        mockMvc.perform(get("/v1/analytics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId")
                        .value("TXN1001"))
                .andExpect(jsonPath("$[1].transactionId")
                        .value("TXN1002"));
    }

    @Test
    void shouldReturnEmptyListWhenNoRecords() throws Exception {

        when(repository.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/analytics"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnAnalyticsCount() throws Exception {

        when(repository.count())
                .thenReturn(5L);

        mockMvc.perform(get("/v1/analytics/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void shouldReturnZeroCountWhenNoRecords() throws Exception {

        when(repository.count())
                .thenReturn(0L);

        mockMvc.perform(get("/v1/analytics/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}