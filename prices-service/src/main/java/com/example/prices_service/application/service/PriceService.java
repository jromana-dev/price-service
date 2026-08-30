package com.example.prices_service.application.service;

import org.springframework.stereotype.Service;

import com.example.prices_service.domain.exception.PriceNotFoundException;
import com.example.prices_service.domain.model.Price;
import com.example.prices_service.domain.port.in.GetApplicablePriceQuery;
import com.example.prices_service.domain.port.in.GetApplicablePriceUseCase;
import com.example.prices_service.domain.port.out.PriceRepositoryPort;

@Service
public class PriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    public PriceService(PriceRepositoryPort priceRepositoryPort) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Price getApplicablePrice(GetApplicablePriceQuery query) {
        return priceRepositoryPort
                .findApplicablePrice(query.brandId(), query.productId(), query.applicationDate())
                .orElseThrow(() -> new PriceNotFoundException(
                        query.brandId(), query.productId(), query.applicationDate()));
    }
}