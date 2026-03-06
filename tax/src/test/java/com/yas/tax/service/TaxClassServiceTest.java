package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class TaxClassServiceTest {

    private TaxClassRepository taxClassRepository;
    private TaxClassService taxClassService;
    private TaxClass taxClass;
    private final Long MOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        taxClassRepository = mock(TaxClassRepository.class);
        taxClassService = new TaxClassService(taxClassRepository);
        taxClass = Instancio.create(TaxClass.class);
        taxClass.setId(MOCK_ID);
    }

    @Test
    void create_shouldThrowDuplicatedException_whenNameExists() {
        // Dùng Instancio để tạo VM, tránh lỗi constructor undefined
        TaxClassPostVm postVm = Instancio.create(TaxClassPostVm.class);
        
        when(taxClassRepository.existsByName(anyString())).thenReturn(true);
        
        assertThatThrownBy(() -> taxClassService.create(postVm))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void update_shouldThrowDuplicatedException_whenNameExistsInOtherRecord() {
        TaxClassPostVm postVm = Instancio.create(TaxClassPostVm.class);
        
        when(taxClassRepository.findById(MOCK_ID)).thenReturn(Optional.of(taxClass));
        // Giả lập tên mới trùng với một bản ghi khác (không phải bản ghi đang update)
        when(taxClassRepository.existsByNameNotUpdatingTaxClass(anyString(), eq(MOCK_ID))).thenReturn(true);
        
        assertThatThrownBy(() -> taxClassService.update(postVm, MOCK_ID))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void delete_shouldDelete_whenExisted() {
        when(taxClassRepository.existsById(MOCK_ID)).thenReturn(true);
        taxClassService.delete(MOCK_ID);
        verify(taxClassRepository).deleteById(MOCK_ID);
    }
}