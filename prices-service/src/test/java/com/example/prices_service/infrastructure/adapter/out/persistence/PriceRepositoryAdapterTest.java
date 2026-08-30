package com.example.prices_service.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.example.prices_service.domain.model.Price;

@DataJpaTest
@Import(PriceRepositoryAdapter.class)
class PriceRepositoryAdapterTest {

    @Autowired
    private PriceRepositoryAdapter priceRepositoryAdapter;

    @Test
    void shouldReturnBasePriceRateAt10AmOnJune14() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(1);
        assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    void shouldReturnHigherPriorityRateWhenTwoRatesOverlap() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L, 35455L, LocalDateTime.of(2020, 6, 14, 16, 0, 0));

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(2);
        assertThat(result.get().priority()).isEqualTo(1);
        assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("25.45"));
    }

    @Test
    void shouldReturnEmptyWhenNoRateMatches() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L, 35455L, LocalDateTime.of(2019, 1, 1, 0, 0, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAnUnknownBrand() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                999L, 35455L, LocalDateTime.of(2020, 6, 14, 10, 0, 0));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnBasePriceRateAt9PmOnJune14() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L,
                35455L,
                LocalDateTime.of(2020, 6, 14, 21, 0, 0)
        );

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(1);
        assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("35.50"));
    }

    @Test
    void shouldReturnThirdRateAt10AmOnJune15() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L,
                35455L,
                LocalDateTime.of(2020, 6, 15, 10, 0, 0)
        );

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(3);
        assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("30.50"));
    }

    @Test
    void shouldReturnFourthRateAt9PmOnJune16() {
        Optional<Price> result = priceRepositoryAdapter.findApplicablePrice(
                1L,
                35455L,
                LocalDateTime.of(2020, 6, 16, 21, 0, 0)
        );

        assertThat(result).isPresent();
        assertThat(result.get().priceList()).isEqualTo(4);
        assertThat(result.get().price()).isEqualByComparingTo(new BigDecimal("38.95"));
    }
}
