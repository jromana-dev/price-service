package com.example.prices_service.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.prices_service.domain.exception.PriceNotFoundException;
import com.example.prices_service.domain.model.Price;
import com.example.prices_service.domain.port.in.GetApplicablePriceQuery;
import com.example.prices_service.domain.port.out.PriceRepositoryPort;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @Test
    void shouldReturnApplicablePriceWhenRepositoryFindsOne() {
        PriceService priceService = new PriceService(priceRepositoryPort);
        LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 10, 0, 0);
        Price expectedPrice = new Price(1L, applicationDate.minusHours(1), applicationDate.plusHours(1),
                1, 35455L, 0, new BigDecimal("35.50"), "EUR");

        when(priceRepositoryPort.findApplicablePrice(1L, 35455L, applicationDate))
                .thenReturn(Optional.of(expectedPrice));

        GetApplicablePriceQuery query = new GetApplicablePriceQuery(1L, 35455L, applicationDate);
        Price result = priceService.getApplicablePrice(query);

        assertThat(result).isEqualTo(expectedPrice);
        verify(priceRepositoryPort).findApplicablePrice(1L, 35455L, applicationDate);
    }

    @Test
    void shouldThrowPriceNotFoundExceptionWhenRepositoryFindsNothing() {
        PriceService priceService = new PriceService(priceRepositoryPort);
        LocalDateTime applicationDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        GetApplicablePriceQuery query = new GetApplicablePriceQuery(1L, 99999L, applicationDate);

        when(priceRepositoryPort.findApplicablePrice(1L, 99999L, applicationDate))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceService.getApplicablePrice(query))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("99999");

        verify(priceRepositoryPort).findApplicablePrice(1L, 99999L, applicationDate);
    }
}

