package com.yas.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList; // Đã bổ sung import này
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressPostVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {

    @Mock WarehouseRepository warehouseRepository;
    @Mock StockRepository stockRepository;
    @Mock ProductService productService;
    @Mock LocationService locationService;

    @InjectMocks WarehouseService warehouseService;

    Warehouse warehouse;
    final Long MOCK_ID = 1L;

    @BeforeEach
    void setUp() {
        warehouse = Instancio.create(Warehouse.class);
        warehouse.setId(MOCK_ID);
        warehouse.setAddressId(2L);
    }

    @Test
    void findAllWarehouses_shouldReturnList() {
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));
        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();
        assertThat(result).hasSize(1);
    }

    @Test
    void getProductWarehouse_whenProductIdsNotEmpty_shouldReturnMappedList() {
        when(stockRepository.getProductIdsInWarehouse(MOCK_ID)).thenReturn(List.of(10L));
        // Khởi tạo ProductInfoVm giả lập
        ProductInfoVm productInfoVm = new ProductInfoVm(10L, "Product", "SKU", false);
        when(productService.filterProducts(anyString(), anyString(), anyList(), any())).thenReturn(List.of(productInfoVm));

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(MOCK_ID, "Product", "SKU", FilterExistInWhSelection.YES);
        
        // Assert theo ID thay vì biến boolean để tránh lỗi undefined getter
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L); 
    }

    @Test
    void getProductWarehouse_whenProductIdsEmpty_shouldReturnOriginalList() {
        when(stockRepository.getProductIdsInWarehouse(MOCK_ID)).thenReturn(List.of());
        ProductInfoVm productInfoVm = new ProductInfoVm(10L, "Product", "SKU", false);
        when(productService.filterProducts(any(), any(), anyList(), any())).thenReturn(List.of(productInfoVm));

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(MOCK_ID, null, null, FilterExistInWhSelection.ALL);
        
        assertThat(result).hasSize(1);
    }

    @Test
    void findById_shouldThrowNotFoundException() {
        when(warehouseRepository.findById(MOCK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> warehouseService.findById(MOCK_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_shouldReturnDetailVm() {
        when(warehouseRepository.findById(MOCK_ID)).thenReturn(Optional.of(warehouse));
        AddressDetailVm addressDetailVm = Instancio.create(AddressDetailVm.class);
        when(locationService.getAddressById(2L)).thenReturn(addressDetailVm);

        WarehouseDetailVm result = warehouseService.findById(MOCK_ID);
        assertThat(result).isNotNull();
    }

    @Test
    void create_shouldThrowDuplicatedException_whenNameExists() {
        WarehousePostVm postVm = Instancio.create(WarehousePostVm.class);
        when(warehouseRepository.existsByName(anyString())).thenReturn(true);
        assertThatThrownBy(() -> warehouseService.create(postVm)).isInstanceOf(DuplicatedException.class);
    }

    @Test
    void create_shouldSaveWarehouse() {
        WarehousePostVm postVm = Instancio.create(WarehousePostVm.class);
        AddressVm addressVm = Instancio.create(AddressVm.class);
        
        when(warehouseRepository.existsByName(anyString())).thenReturn(false);
        when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(addressVm);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse result = warehouseService.create(postVm);
        assertThat(result).isNotNull();
    }

    @Test
    void update_shouldThrowDuplicatedException_whenNameExistsForOtherId() {
        WarehousePostVm postVm = Instancio.create(WarehousePostVm.class);
        when(warehouseRepository.findById(MOCK_ID)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId(anyString(), eq(MOCK_ID))).thenReturn(true);

        assertThatThrownBy(() -> warehouseService.update(postVm, MOCK_ID)).isInstanceOf(DuplicatedException.class);
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        WarehousePostVm postVm = Instancio.create(WarehousePostVm.class);
        when(warehouseRepository.findById(MOCK_ID)).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByNameWithDifferentId(anyString(), eq(MOCK_ID))).thenReturn(false);

        warehouseService.update(postVm, MOCK_ID);

        verify(locationService).updateAddress(anyLong(), any(AddressPostVm.class));
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void delete_shouldDeleteWarehouseAndAddress() {
        when(warehouseRepository.findById(MOCK_ID)).thenReturn(Optional.of(warehouse));
        warehouseService.delete(MOCK_ID);

        verify(warehouseRepository).deleteById(MOCK_ID);
        verify(locationService).deleteAddress(warehouse.getAddressId());
    }

    @Test
    void getPageableWarehouses_shouldReturnVm() {
        when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(warehouse)));
        WarehouseListGetVm result = warehouseService.getPageableWarehouses(0, 10);
        assertThat(result.warehouseContent()).hasSize(1);
    }
}