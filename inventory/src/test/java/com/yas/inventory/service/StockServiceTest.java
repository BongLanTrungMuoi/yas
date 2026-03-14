package com.yas.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
import java.util.List;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

    @Mock WarehouseRepository warehouseRepository;
    @Mock StockRepository stockRepository;
    @Mock ProductService productService;
    @Mock WarehouseService warehouseService;
    @Mock StockHistoryService stockHistoryService;

    @InjectMocks StockService stockService;

    @Test
    void addProductIntoWarehouse_shouldThrowStockExistingException() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> stockService.addProductIntoWarehouse(List.of(postVm)))
            .isInstanceOf(StockExistingException.class);
    }

    @Test
    void addProductIntoWarehouse_shouldThrowNotFound_whenProductNull() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(null);

        assertThatThrownBy(() -> stockService.addProductIntoWarehouse(List.of(postVm)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addProductIntoWarehouse_shouldThrowNotFound_whenWarehouseEmpty() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(Instancio.create(ProductInfoVm.class));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.addProductIntoWarehouse(List.of(postVm)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addProductIntoWarehouse_shouldSaveAll() {
        StockPostVm postVm = new StockPostVm(1L, 1L);
        when(stockRepository.existsByWarehouseIdAndProductId(1L, 1L)).thenReturn(false);
        when(productService.getProduct(1L)).thenReturn(Instancio.create(ProductInfoVm.class));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(new Warehouse()));

        stockService.addProductIntoWarehouse(List.of(postVm));

        verify(stockRepository).saveAll(anyList());
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_shouldReturnMappedList() {
        ProductInfoVm productInfoVm = new ProductInfoVm(10L, "Name", "SKU", true);
        when(warehouseService.getProductWarehouse(anyLong(), any(), any(), any())).thenReturn(List.of(productInfoVm));

        Stock stock = Instancio.create(Stock.class);
        stock.setProductId(10L); // Phải khớp với Product ID ở trên
        when(stockRepository.findByWarehouseIdAndProductIdIn(anyLong(), anyList())).thenReturn(List.of(stock));

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(1L, "Name", "SKU");
        assertThat(result).hasSize(1);
    }

    @Test
    void updateProductQuantityInStock_shouldUpdateSuccessfully() {
        StockQuantityVm quantityVm = new StockQuantityVm(1L, 5L, "Restock");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(quantityVm));

        Stock stock = Instancio.create(Stock.class);
        stock.setId(1L);
        stock.setQuantity(10L);
        when(stockRepository.findAllById(anyList())).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(requestBody);

        verify(stockRepository).saveAll(anyList());
        verify(stockHistoryService).createStockHistories(anyList(), anyList());
        verify(productService).updateProductQuantity(anyList());
    }
}