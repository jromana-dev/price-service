package com.example.prices_service.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.prices_service.domain.model.Price;
import com.example.prices_service.domain.port.out.PriceRepositoryPort;
import com.example.prices_service.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper;
import com.example.prices_service.infrastructure.adapter.out.persistence.repository.SpringDataPriceRepository;

@Repository
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private final SpringDataPriceRepository springDataPriceRepository;

    public PriceRepositoryAdapter(SpringDataPriceRepository springDataPriceRepository) {
        this.springDataPriceRepository = springDataPriceRepository;
    }

    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        return springDataPriceRepository
                .findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        brandId, productId, applicationDate, applicationDate)
                .map(PriceEntityMapper::toDomain);
    }
}