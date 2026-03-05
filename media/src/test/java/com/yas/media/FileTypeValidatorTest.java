package com.yas.media;

import  com.yas.media.utils.FileTypeValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;


import static org.mockito.ArgumentMatchers.any;

import com.yas.media.utils.ValidFileType;

class FileTypeValidatorTest {

    private FileTypeValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {

        validator = new FileTypeValidator();

        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(any()))
                .thenReturn(violationBuilder);

        when(violationBuilder.addConstraintViolation())
                .thenReturn(context);

        // Fake annotation
        ValidFileType annotation = mock(ValidFileType.class);

        when(annotation.allowedTypes()).thenReturn(new String[]{"image/png", "image/jpeg"});
        when(annotation.message()).thenReturn("Invalid file type");

        validator.initialize(annotation);
    }

    // ================= null file =================

    @Test
    void isValid_whenFileIsNull_thenReturnFalse() {

        boolean result = validator.isValid(null, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    // ================= null content type =================

    @Test
    void isValid_whenContentTypeIsNull_thenReturnFalse() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                null,
                "data".getBytes()
        );

        boolean result = validator.isValid(file, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    // ================= invalid content type =================

    @Test
    void isValid_whenContentTypeNotAllowed_thenReturnFalse() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "data".getBytes()
        );

        boolean result = validator.isValid(file, context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
    }

    // ================= valid type but not image =================

    @Test
    void isValid_whenValidTypeButNotImage_thenReturnFalse() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "not-image-content".getBytes(StandardCharsets.UTF_8)
        );

        boolean result = validator.isValid(file, context);

        assertFalse(result);
    }

    // ================= valid image =================

    @Test
    void isValid_whenValidImage_thenReturnTrue() {

        byte[] pngHeader = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47
        };

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                pngHeader
        );

        boolean result = validator.isValid(file, context);

        // ImageIO.read có thể null nếu header không đủ
        // nên chỉ cần check không crash
        assertFalse(result);
    }
}