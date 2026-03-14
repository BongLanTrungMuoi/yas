package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = TaxRateService.class)
public class TaxServiceTest {
    
    @MockBean
    TaxRateRepository taxRateRepository;
    @MockBean
    LocationService locationService;
    @MockBean
    TaxClassRepository taxClassRepository;

    @Autowired
    TaxRateService taxRateService;

    TaxRate taxRate;
    TaxClass taxClass;

    final Long MOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        taxClass = Instancio.of(TaxClass.class)
            .set(field("id"), MOCK_ID)
            .create();
            
        taxRate = Instancio.of(TaxRate.class)
            .set(field("id"), MOCK_ID)
            .set(field("taxClass"), taxClass)
            .create();
            
        lenient().when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));
        
        // Mock chuẩn xác cho các hàm kiểm tra ID
        lenient().when(taxClassRepository.findById(anyLong())).thenReturn(Optional.of(taxClass));
        lenient().when(taxClassRepository.existsById(anyLong())).thenReturn(true);
        
        lenient().when(taxRateRepository.findById(anyLong())).thenReturn(Optional.of(taxRate));
        lenient().when(taxRateRepository.existsById(anyLong())).thenReturn(true);
    }

    @Test
    void testFindAll_shouldReturnAllTaxRate() {
        List<TaxRateVm> result = taxRateService.findAll();
        assertThat(result).hasSize(1).contains(TaxRateVm.fromModel(taxRate));
    }

    @Test
    void findById_shouldReturnTaxRateVm_whenExists() {
        TaxRateVm result = taxRateService.findById(MOCK_ID);
        assertThat(result).isNotNull();
    }

    @Test
    void findById_shouldThrowNotFoundException_whenNotExists() {
        lenient().when(taxRateRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(taxRateRepository.existsById(anyLong())).thenReturn(false);
        
        assertThatThrownBy(() -> taxRateService.findById(MOCK_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createTaxRate_shouldReturnSavedTaxRate_whenValidInput() {
        TaxRatePostVm postVm = Instancio.of(TaxRatePostVm.class)
            .set(field("taxClassId"), MOCK_ID)
            .create();
            
        lenient().when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        TaxRate result = taxRateService.createTaxRate(postVm);

        assertThat(result).isNotNull();
        verify(taxRateRepository).save(any(TaxRate.class));
    }

    @Test
    void updateTaxRate_shouldUpdateSuccessfully_whenValidInput() {
        TaxRatePostVm putVm = Instancio.of(TaxRatePostVm.class)
            .set(field("taxClassId"), MOCK_ID)
            .create();
            
        lenient().when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        taxRateService.updateTaxRate(putVm, MOCK_ID);

        verify(taxRateRepository).save(any(TaxRate.class));
    }

    @Test
    void delete_shouldDeleteSuccessfully_whenTaxRateExists() {
        taxRateService.delete(MOCK_ID);
    }
}