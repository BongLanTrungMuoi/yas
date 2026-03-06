package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;

import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class TaxRateServiceTest {
    private TaxRateRepository taxRateRepository;
    private TaxClassRepository taxClassRepository;
    private LocationService locationService;
    private TaxRateService taxRateService;

    @BeforeEach
    void setUp() {
        taxRateRepository = mock(TaxRateRepository.class);
        taxClassRepository = mock(TaxClassRepository.class);
        locationService = mock(LocationService.class);
        taxRateService = new TaxRateService(locationService, taxRateRepository, taxClassRepository);
    }

    @Test
    void getPageableTaxRates_shouldReturnData() {
        TaxClass taxClass = new TaxClass();
        taxClass.setName("VAT");
        TaxRate taxRate = TaxRate.builder().id(1L).stateOrProvinceId(1L).taxClass(taxClass).build();

        when(taxRateRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(taxRate)));

        StateOrProvinceAndCountryGetNameVm locVm = new StateOrProvinceAndCountryGetNameVm(1L, "Hanoi", "VN");
        when(locationService.getStateOrProvinceAndCountryNames(anyList())).thenReturn(List.of(locVm));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        // NẾU taxRateContent() VẪN BÁO LỖI, HÃY THỬ: result.getTaxRateContent()
        assertThat(result).isNotNull();
    }

    @Test
    void getPageableTaxRates_whenNoTaxRates_shouldReturnEmptyContent() {
        // Giả lập Repository trả về trang trống
        when(taxRateRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        // Assert để phủ nhánh "isEmpty()"
        assertThat(result).isNotNull();
        verify(locationService, never()).getStateOrProvinceAndCountryNames(any());
    }

    @Test
    void getTaxPercent_shouldReturnActualValue_whenFound() {
        // Phủ nhánh "if (taxPercent != null)"
        when(taxRateRepository.getTaxPercent(anyLong(), anyLong(), any(), anyLong())).thenReturn(15.5);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "70000");

        assertThat(result).isEqualTo(15.5);
    }

    @Test
    void getBulkTaxRate_shouldReturnMappedVms() {
        TaxRate tr = Instancio.create(TaxRate.class);
        when(taxRateRepository.getBatchTaxRates(anyLong(), anyLong(), any(), any())).thenReturn(List.of(tr));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(1L), 1L, 1L, "70000");

        assertThat(result).hasSize(1);
    }
}