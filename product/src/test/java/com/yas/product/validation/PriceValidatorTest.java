package com.yas.product.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceValidatorTest {

    private PriceValidator priceValidator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        priceValidator = new PriceValidator();
    }

    @Test
    void isValid_whenPriceIsPositive_shouldReturnTrue() {
        assertTrue(priceValidator.isValid(10.0, constraintValidatorContext));
    }

    @Test
    void isValid_whenPriceIsZero_shouldReturnTrue() {
        assertTrue(priceValidator.isValid(0.0, constraintValidatorContext));
    }

    @Test
    void isValid_whenPriceIsNegative_shouldReturnFalse() {
        assertFalse(priceValidator.isValid(-1.0, constraintValidatorContext));
    }

    @Test
    void isValid_whenPriceIsLargePositiveValue_shouldReturnTrue() {
        assertTrue(priceValidator.isValid(999999.99, constraintValidatorContext));
    }

    @Test
    void isValid_whenPriceIsSmallNegativeValue_shouldReturnFalse() {
        assertFalse(priceValidator.isValid(-0.01, constraintValidatorContext));
    }

    @Test
    void isValid_whenPriceIsVerySmallPositive_shouldReturnTrue() {
        assertTrue(priceValidator.isValid(0.01, constraintValidatorContext));
    }
}
