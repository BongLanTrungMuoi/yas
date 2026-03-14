package com.yas.tax.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.tax.model.TaxClass;
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
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
public class TaxClassControllerTest {

    @Mock
    TaxClassService taxClassService;

    @InjectMocks
    TaxClassController taxClassController;

    TaxClassVm taxClassVm;
    TaxClass taxClass;
    final Long MOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        taxClassVm = Instancio.create(TaxClassVm.class);
        taxClass = Instancio.of(TaxClass.class)
            .set(field("id"), MOCK_ID)
            .create();
    }

    @Test
    void getPageableTaxClasses_shouldReturnOk() {
        TaxClassListGetVm listGetVm = Instancio.create(TaxClassListGetVm.class);
        when(taxClassService.getPageableTaxClasses(anyInt(), anyInt())).thenReturn(listGetVm);

        ResponseEntity<TaxClassListGetVm> response = taxClassController.getPageableTaxClasses(0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTaxClass_shouldReturnOk() {
        when(taxClassService.findById(MOCK_ID)).thenReturn(taxClassVm);

        ResponseEntity<TaxClassVm> response = taxClassController.getTaxClass(MOCK_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void createTaxClass_shouldReturnCreated() {
        TaxClassPostVm postVm = Instancio.create(TaxClassPostVm.class);
        when(taxClassService.create(any(TaxClassPostVm.class))).thenReturn(taxClass);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<TaxClassVm> response = taxClassController.createTaxClass(postVm, uriBuilder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateTaxClass_shouldReturnNoContent() {
        TaxClassPostVm putVm = Instancio.create(TaxClassPostVm.class);

        ResponseEntity<Void> response = taxClassController.updateTaxClass(MOCK_ID, putVm);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taxClassService).update(any(TaxClassPostVm.class), eq(MOCK_ID));
    }

    @Test
    void deleteTaxClass_shouldReturnNoContent() {
        ResponseEntity<Void> response = taxClassController.deleteTaxClass(MOCK_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taxClassService).delete(MOCK_ID);
    }
}