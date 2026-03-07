package com.yas.product.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import com.yas.product.utils.MessagesUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesUtilsTest {

    // Class giả lập nội dung bundle
    private static class TestBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                {"hello.key", "Hello {}!"},
                {"error.id", "Product id {} not found"}
            };
        }
    }

    @BeforeEach
    void setUp() {
        // Khởi tạo trực tiếp object thay vì dùng ResourceBundle.getBundle()
        ResourceBundle mockBundle = new TestBundle();
        
        // "Bơm" trực tiếp vào field static của MessagesUtils
        ReflectionTestUtils.setField(MessagesUtils.class, "messageBundle", mockBundle);
    }

    @Test
    @DisplayName("getMessage: Định dạng tham số thành công")
    void getMessage_Success() {
        String result = MessagesUtils.getMessage("hello.key", "Gemini");
        assertEquals("Hello Gemini!", result);
    }

    @Test
    @DisplayName("getMessage: Trả về key khi không tìm thấy")
    void getMessage_NotFound() {
        String result = MessagesUtils.getMessage("unknown.key");
        assertEquals("unknown.key", result);
    }
}