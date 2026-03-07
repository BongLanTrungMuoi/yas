package com.yas.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class ValidateProductPriceTest {

    @Test
    void testAnnotationConfiguration() {
        Constraint constraint = ValidateProductPrice.class.getAnnotation(Constraint.class);

        assertNotNull(constraint);
        assertEquals(PriceValidator.class, constraint.validatedBy()[0]);
    }

    @Test
    void testDefaultMessage() throws NoSuchFieldException {
        class TestClass {
            @ValidateProductPrice
            Double price;
        }

        Annotation annotation = TestClass.class
                .getDeclaredField("price")
                .getAnnotation(ValidateProductPrice.class);

        ValidateProductPrice validate = (ValidateProductPrice) annotation;

        assertEquals("Price must greater than 0", validate.message());
    }

    @Test
    void testDefaultGroups() throws NoSuchFieldException {
        class TestClass {
            @ValidateProductPrice
            Double price;
        }

        ValidateProductPrice validate = TestClass.class
                .getDeclaredField("price")
                .getAnnotation(ValidateProductPrice.class);

        assertEquals(0, validate.groups().length);
    }

    @Test
    void testDefaultPayload() throws NoSuchFieldException {
        class TestClass {
            @ValidateProductPrice
            Double price;
        }

        ValidateProductPrice validate = TestClass.class
                .getDeclaredField("price")
                .getAnnotation(ValidateProductPrice.class);

        assertEquals(0, validate.payload().length);
    }
}