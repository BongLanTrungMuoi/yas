package com.yas.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractCircuitBreakFallbackHandlerTest {

    // Tạo một class con giả lập để test vì đây là abstract class
    private static class TestHandler extends AbstractCircuitBreakFallbackHandler {
    }

    private TestHandler testHandler;

    @BeforeEach
    void setUp() {
        testHandler = new TestHandler();
    }

    @Test
    @DisplayName("handleBodilessFallback: Phải ném lại đúng Exception nhận vào")
    void handleBodilessFallback_ShouldThrowException() {
        RuntimeException originalException = new RuntimeException("Circuit breaker error");

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            testHandler.handleBodilessFallback(originalException);
        });

        assertEquals("Circuit breaker error", thrownException.getMessage());
    }

    @Test
    @DisplayName("handleTypedFallback: Phải ném lại đúng Exception nhận vào thay vì trả về null")
    void handleTypedFallback_ShouldThrowException() {
        IllegalArgumentException originalException = new IllegalArgumentException("Invalid data");

        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
            testHandler.handleTypedFallback(originalException);
        });

        assertEquals("Invalid data", thrownException.getMessage());
    }

    @Test
    @DisplayName("handleError: Đảm bảo exception gốc được giữ nguyên")
    void handleError_ShouldPreserveOriginalException() {
        Exception originalException = new Exception("Critical system failure");

        Exception thrownException = assertThrows(Exception.class, () -> {
            testHandler.handleError(originalException);
        });

        assertEquals("Critical system failure", thrownException.getMessage());
    }
}