package com.example.prices_service.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(Long brandId, Long productId, LocalDateTime applicationDate) {
        super("No applicable price found for brandId=%s, productId=%s, applicationDate=%s"
                .formatted(brandId, productId, applicationDate));
    }
}
