package com.example.prices_service.domain.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.prices_service.domain.model.Price;

/**
 * Port for retrieving applicable prices.
 */
public interface PriceRepositoryPort {

    /**
     * Finds the highest-priority price applicable at the given date.
     *
     * @param brandId brand identifier
     * @param productId product identifier
     * @param applicationDate date and time for the price lookup
     * @return the applicable price, or {@link Optional#empty()} if none exists
     */
    Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate);
}
