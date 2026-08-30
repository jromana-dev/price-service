package com.example.prices_service.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    private static final String ENDPOINT = "/api/v1/prices";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnFirstRateAt10AmOnJune14() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T00:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59"));
    }

    @Test
    void shouldReturnSecondRateAt4PmOnJune14() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2020-06-14T16:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.price").value(25.45))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T15:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-06-14T18:30:00"));
    }

    @Test
    void shouldReturnFirstRateAt9PmOnJune14() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2020-06-14T21:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    void shouldReturnThirdRateAt10AmOnJune15() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2020-06-15T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(3))
                .andExpect(jsonPath("$.price").value(30.50));
    }

    @Test
    void shouldReturnFourthRateAt9PmOnJune16() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2020-06-16T21:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceList").value(4))
                .andExpect(jsonPath("$.price").value(38.95));
    }

    @Test
    void shouldReturn404WhenNoRateIsApplicable() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "2019-01-01T00:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenApplicationDateIsMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenApplicationDateHasInvalidFormat() throws Exception {
        mockMvc.perform(get(ENDPOINT)
                        .param("applicationDate", "not-a-date")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isBadRequest());
    }
}
