package com.yas.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StockHistoryServiceTest {

    @Mock
    StockHistoryRepository stockHistoryRepository;
    @Mock
    ProductService productService;

    @InjectMocks
    StockHistoryService stockHistoryService;

    @Test
    void createStockHistories_shouldSaveAll_whenMatchFound() {
        Stock stock = Instancio.create(Stock.class);
        stock.setId(1L);

        StockQuantityVm quantityVm = new StockQuantityVm(1L, 10L, "Note");

        stockHistoryService.createStockHistories(List.of(stock), List.of(quantityVm));

        verify(stockHistoryRepository).saveAll(anyList());
    }

    @Test
    void getStockHistories_shouldReturnList() {
        StockHistory history = Instancio.create(StockHistory.class);
        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(anyLong(), anyLong()))
                .thenReturn(List.of(history));

        ProductInfoVm productInfoVm = Instancio.create(ProductInfoVm.class);
        when(productService.getProduct(anyLong())).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(1L, 1L);

        assertThat(result).isNotNull();
    }
}