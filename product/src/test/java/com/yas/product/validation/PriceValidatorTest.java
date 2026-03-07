package com.yas.product.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PriceValidatorTest {

    private PriceValidator priceValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        priceValidator = new PriceValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_WhenPricePositive_ReturnsTrue() {
        boolean result = priceValidator.isValid(100.0, context);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_WhenPriceZero_ReturnsTrue() {
        boolean result = priceValidator.isValid(0.0, context);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_WhenPriceNegative_ReturnsFalse() {
        boolean result = priceValidator.isValid(-10.0, context);

        assertThat(result).isFalse();
    }
}