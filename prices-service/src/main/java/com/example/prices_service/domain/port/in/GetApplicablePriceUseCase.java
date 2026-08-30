package com.example.prices_service.domain.port.in;

import com.example.prices_service.domain.model.Price;

/**
 * Entry point for retrieving the applicable price for a product.
 */
public interface GetApplicablePriceUseCase {

    Price getApplicablePrice(GetApplicablePriceQuery query);
}
