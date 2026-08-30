package com.example.prices_service.infrastructure.adapter.out.persistence.mapper;

import com.example.prices_service.domain.model.Price;
import com.example.prices_service.infrastructure.adapter.out.persistence.entity.PriceEntity;

public final class PriceEntityMapper {

    private PriceEntityMapper() {
    }

    public static Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriceList(),
                entity.getProductId(),
                entity.getPriority(),
                entity.getPrice(),
                entity.getCurr()
        );
    }
}
