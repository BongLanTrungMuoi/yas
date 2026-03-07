package com.yas.product.util; // Phải khớp với thư mục chứa file test

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Import class ProductConverter từ source code chính
import com.yas.product.utils.ProductConverter; 

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductConverterTest {

    @ParameterizedTest
    @DisplayName("toSlug: Chuyển đổi tên sản phẩm sang Slug chuẩn")
    @CsvSource({
        "'iPhone 15 Pro', 'iphone-15-pro'",
        "'Samsung Galaxy S24+', 'samsung-galaxy-s24-'",
        "'  Trim  Space  ', 'trim-space'",
        "'Double--Dash', 'double-dash'",
        "'-StartWithDash', 'startwithdash'",
        "'Laptop & MacBook', 'laptop-macbook'"
    })
    void toSlug_ShouldReturnCorrectSlug(String input, String expected) {
        // Nếu vẫn báo lỗi, hãy thử gọi trực tiếp: com.yas.product.utils.ProductConverter.toSlug(input)
        String result = ProductConverter.toSlug(input);
        assertEquals(expected, result);
    }
}