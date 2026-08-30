package com.example.prices_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Price(
        Long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priceList,
        Long productId,
        Integer priority,
        BigDecimal price,
        String currency
) {
}
