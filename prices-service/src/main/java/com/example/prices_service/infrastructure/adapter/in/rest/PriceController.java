package com.example.prices_service.infrastructure.adapter.in.rest;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.prices_service.domain.model.Price;
import com.example.prices_service.domain.port.in.GetApplicablePriceQuery;
import com.example.prices_service.domain.port.in.GetApplicablePriceUseCase;
import com.example.prices_service.infrastructure.adapter.in.rest.dto.PriceResponse;
import com.example.prices_service.infrastructure.adapter.in.rest.mapper.PriceRestMapper;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/prices")
@Validated
public class PriceController {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    public PriceController(GetApplicablePriceUseCase getApplicablePriceUseCase) {
        this.getApplicablePriceUseCase = getApplicablePriceUseCase;
    }


    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime applicationDate,
            @RequestParam @NotNull Long productId,
            @RequestParam @NotNull Long brandId
    ) {
        Price price = getApplicablePriceUseCase.getApplicablePrice(
                new GetApplicablePriceQuery(brandId, productId, applicationDate));

        return ResponseEntity.ok(PriceRestMapper.toResponse(price));
    }
}
