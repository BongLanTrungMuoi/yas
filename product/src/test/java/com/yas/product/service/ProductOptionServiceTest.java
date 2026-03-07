package com.yas.product.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.ProductOption;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.viewmodel.productoption.ProductOptionListGetVm;
import com.yas.product.viewmodel.productoption.ProductOptionPostVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;



@ExtendWith(MockitoExtension.class)
class ProductOptionServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;
    @InjectMocks
    private ProductOptionService productOptionService;

    // --- CÁC HÀM TEST CŨ CỦA BẠN (GIỮ NGUYÊN) ---

    @Test
    void test_get_pageable_product_options_success() {
        List<ProductOption> productOptions = List.of(new ProductOption(), new ProductOption());
        Page<ProductOption> productOptionPage = new PageImpl<>(productOptions);
        when(productOptionRepository.findAll(any(Pageable.class))).thenReturn(productOptionPage);

        ProductOptionListGetVm result = productOptionService.getPageableProductOptions(0, 2);

        assertEquals(2, result.productOptionContent().size());
        assertEquals(0, result.pageNo());
        assertEquals(2, result.pageSize());
    }

    @Test
    void test_create_product_option_unique_name() {
        ProductOptionPostVm postVm = new ProductOptionPostVm("UniqueName");
        when(productOptionRepository.findExistedName(anyString(), isNull())).thenReturn(null);
        when(productOptionRepository.save(any(ProductOption.class))).thenReturn(new ProductOption());

        ProductOption result = productOptionService.create(postVm);

        assertNotNull(result);
        verify(productOptionRepository).save(any(ProductOption.class));
    }

    @Test
    void test_update_product_option_unique_name() {
        ProductOption existingProductOption = new ProductOption();
        existingProductOption.setId(1L);
        existingProductOption.setName("OldName");

        ProductOptionPostVm postVm = new ProductOptionPostVm("NewUniqueName");
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(existingProductOption));
        when(productOptionRepository.findExistedName(anyString(), eq(1L))).thenReturn(null);
        when(productOptionRepository.save(any(ProductOption.class))).thenReturn(existingProductOption);

        ProductOption result = productOptionService.update(postVm, 1L);

        assertNotNull(result);
        assertEquals("NewUniqueName", result.getName());
    }

    @Test
    void test_create_product_option_duplicated_name() {
        ProductOptionPostVm postVm = new ProductOptionPostVm("DuplicatedName");
        when(productOptionRepository.findExistedName(anyString(), isNull())).thenReturn(new ProductOption());

        assertThrows(DuplicatedException.class, () -> {
            productOptionService.create(postVm);
        });
    }

    @Test
    void test_update_product_option_duplicated_name() {
        ProductOption existingProductOption = new ProductOption();
        existingProductOption.setId(1L);
        existingProductOption.setName("OldName");

        ProductOptionPostVm postVm = new ProductOptionPostVm("DuplicatedName");
        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(existingProductOption));
        when(productOptionRepository.findExistedName(anyString(), eq(1L))).thenReturn(new ProductOption());

        assertThrows(DuplicatedException.class, () -> {
            productOptionService.update(postVm, 1L);
        });
    }

    @Test
    void test_update_non_existent_product_option() {
        ProductOptionPostVm postVm = new ProductOptionPostVm("NewName");
        when(productOptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            productOptionService.update(postVm, 1L);
        });
    }

    @Test
    void test_get_pageable_product_options_empty_list() {
        Page<ProductOption> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 2), 0);
        when(productOptionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        ProductOptionListGetVm result = productOptionService.getPageableProductOptions(0, 2);

        assertTrue(result.productOptionContent().isEmpty());
        assertEquals(0, result.pageNo());
        assertEquals(2, result.pageSize());
    }

    // --- CÁC HÀM TEST BỔ SUNG ---

    @Test
    void test_get_pageable_product_options_metadata_check() {
        // Kiểm tra các thông tin phân trang như totalPages, totalElements
        ProductOption option = new ProductOption();
        option.setId(1L);
        option.setName("Size");
        
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProductOption> page = new PageImpl<>(List.of(option), pageable, 10);
        
        when(productOptionRepository.findAll(any(Pageable.class))).thenReturn(page);

        ProductOptionListGetVm result = productOptionService.getPageableProductOptions(0, 5);

        assertEquals(10, result.totalElements());
        assertEquals(2, result.totalPages()); // 10 phần tử, size 5 => 2 trang
        assertFalse(result.isLast()); // Trang 0 không phải trang cuối vì còn trang 1
    }

    @Test
    void test_update_product_option_keep_same_name_success() {
        // Trường hợp cập nhật nhưng không đổi tên, logic checkExistedName phải cho qua
        ProductOption existingOption = new ProductOption();
        existingOption.setId(1L);
        existingOption.setName("Color");

        ProductOptionPostVm postVm = new ProductOptionPostVm("Color");

        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(existingOption));
        // Repository trả về null nghĩa là không có bản ghi KHÁC trùng tên
        when(productOptionRepository.findExistedName("Color", 1L)).thenReturn(null);
        when(productOptionRepository.save(any(ProductOption.class))).thenReturn(existingOption);

        ProductOption result = productOptionService.update(postVm, 1L);

        assertNotNull(result);
        assertEquals("Color", result.getName());
        verify(productOptionRepository).save(any(ProductOption.class));
    }
}