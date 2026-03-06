package com.yas.tax.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
public class TaxRateControllerTest {

    @Mock
    TaxRateService taxRateService;

    @InjectMocks
    TaxRateController taxRateController;

    TaxRateVm taxRateVm;
    TaxRate taxRate;
    final Long MOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        taxRateVm = Instancio.create(TaxRateVm.class);

        // Khởi tạo cả TaxClass để tránh NullPointerException khi controller gọi
        // TaxRateVm.fromModel(taxRate)
        TaxClass taxClass = Instancio.create(TaxClass.class);
        taxRate = Instancio.of(TaxRate.class)
                .set(field("id"), MOCK_ID)
                .set(field("taxClass"), taxClass)
                .create();
    }

    @Test
    void getPageableTaxRates_shouldReturnOk() {
        TaxRateListGetVm listGetVm = Instancio.create(TaxRateListGetVm.class);
        when(taxRateService.getPageableTaxRates(anyInt(), anyInt())).thenReturn(listGetVm);

        ResponseEntity<TaxRateListGetVm> response = taxRateController.getPageableTaxRates(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTaxRate_shouldReturnOk() {
        when(taxRateService.findById(MOCK_ID)).thenReturn(taxRateVm);

        ResponseEntity<TaxRateVm> response = taxRateController.getTaxRate(MOCK_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void createTaxRate_shouldReturnCreated() {
        TaxRatePostVm postVm = Instancio.create(TaxRatePostVm.class);
        when(taxRateService.createTaxRate(any(TaxRatePostVm.class))).thenReturn(taxRate);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();

        ResponseEntity<TaxRateVm> response = taxRateController.createTaxRate(postVm, uriBuilder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateTaxRate_shouldReturnNoContent() {
        TaxRatePostVm putVm = Instancio.create(TaxRatePostVm.class);

        ResponseEntity<Void> response = taxRateController.updateTaxRate(MOCK_ID, putVm);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taxRateService).updateTaxRate(any(TaxRatePostVm.class), eq(MOCK_ID));
    }

    @Test
    void deleteTaxRate_shouldReturnNoContent() {
        ResponseEntity<Void> response = taxRateController.deleteTaxRate(MOCK_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taxRateService).delete(MOCK_ID);
    }

    @Test
    void getTaxPercentByAddress_shouldReturnOk() {
        when(taxRateService.getTaxPercent(anyLong(), anyLong(), anyLong(), anyString())).thenReturn(10.0);

        ResponseEntity<Double> response = taxRateController.getTaxPercentByAddress(1L, 1L, 1L, "12345");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(10.0);
    }

    @Test
    void getBatchTaxPercentsByAddress_shouldReturnOk() {
        when(taxRateService.getBulkTaxRate(any(), anyLong(), anyLong(), anyString())).thenReturn(List.of(taxRateVm));

        ResponseEntity<List<TaxRateVm>> response = taxRateController.getBatchTaxPercentsByAddress(List.of(1L), 1L, 1L,
                "12345");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getTaxPercentByAddress_withMinimalParams_shouldReturnOk() {
        when(taxRateService.getTaxPercent(anyLong(), anyLong(), eq(null), eq(null))).thenReturn(5.0);

        ResponseEntity<Double> response = taxRateController.getTaxPercentByAddress(1L, 1L, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5.0);
    }
}