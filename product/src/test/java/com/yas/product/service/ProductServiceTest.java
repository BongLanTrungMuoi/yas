package com.yas.product.service;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.*;
import com.yas.product.repository.*;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductOptionValueRepository productOptionValueRepository;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    private Brand brand;

    @BeforeEach
    void setUp() {
        // Chỉ khởi tạo data mẫu, không thực hiện stubbing (when/then) ở đây
        brand = new Brand();
        brand.setId(1L);
        brand.setName("Sony");
    }

    @Test
    @DisplayName("Tạo sản phẩm thành công khi dữ liệu hợp lệ")
    void createProduct_Success() {

        ProductPostVm productPostVm = mock(ProductPostVm.class);

        when(productPostVm.name()).thenReturn("Product A");
        when(productPostVm.slug()).thenReturn("product-a");
        when(productPostVm.sku()).thenReturn("SKU001");
        when(productPostVm.length()).thenReturn(10.0);
        when(productPostVm.width()).thenReturn(5.0);
        when(productPostVm.brandId()).thenReturn(1L);
        when(productPostVm.categoryIds()).thenReturn(List.of(1L,2L));

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.findBySlugAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue(anyString())).thenReturn(Optional.empty());

        Category category1 = new Category();
        category1.setId(1L);

        Category category2 = new Category();
        category2.setId(2L);

        when(categoryRepository.findAllById(anyList()))
                .thenReturn(List.of(category1, category2));

        Product savedProduct = new Product();
        savedProduct.setId(100L);
        savedProduct.setName("Product A");

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductGetDetailVm result = productService.createProduct(productPostVm);

        assertThat(result).isNotNull();

        verify(productRepository).save(any(Product.class));
        verify(productCategoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Lỗi khi chiều dài nhỏ hơn chiều rộng")
    void createProduct_LengthSmallerThanWidth_ThrowsBadRequest() {
        ProductPostVm productPostVm = mock(ProductPostVm.class);
        when(productPostVm.length()).thenReturn(5.0);
        when(productPostVm.width()).thenReturn(10.0);

        assertThatThrownBy(() -> productService.createProduct(productPostVm))
                .isInstanceOf(BadRequestException.class);
        
        // Mockito sẽ không phàn nàn vì các stubbing khác không hề được gọi ở đây
    }

    @Test
    @DisplayName("Lấy chi tiết sản phẩm thành công")
    void getProductById_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setThumbnailMediaId(10L);

        NoFileMediaVm mockMedia = mock(NoFileMediaVm.class);
        when(mockMedia.url()).thenReturn("http://image.url");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(mockMedia);

        var result = productService.getProductById(1L);

        assertThat(result.id()).isEqualTo(1L);
        verify(mediaService).getMedia(10L);
    }

    @Test
    @DisplayName("Xóa sản phẩm thành công")
    void deleteProduct_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setPublished(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertThat(product.isPublished()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Trừ số lượng tồn kho thành công")
    void subtractStockQuantity_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(20L);
        product.setStockTrackingEnabled(true);

        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));
        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);

        productService.subtractStockQuantity(List.of(item));

        assertThat(product.getStockQuantity()).isEqualTo(15L);
        verify(productRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Lỗi NotFound khi cập nhật sản phẩm không tồn tại")
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        ProductPutVm putVm = mock(ProductPutVm.class);

        assertThatThrownBy(() -> productService.updateProduct(999L, putVm))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createProduct_DuplicateSlug_ThrowsException() {

        ProductPostVm vm = mock(ProductPostVm.class);

        when(vm.slug()).thenReturn("product-a");
        when(productRepository.findBySlugAndIsPublishedTrue("product-a"))
                .thenReturn(Optional.of(new Product()));

        // assertThatThrownBy(() -> productService.createProduct(vm))
        //         .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> productService.createProduct(vm))
        .isInstanceOf(DuplicatedException.class);
    }
    @Test
    void createProduct_DuplicateSku_ThrowsException() {

        ProductPostVm vm = mock(ProductPostVm.class);

        when(vm.slug()).thenReturn("product-a");
        when(vm.sku()).thenReturn("SKU001");

        when(productRepository.findBySlugAndIsPublishedTrue(anyString()))
                .thenReturn(Optional.empty());

        when(productRepository.findBySkuAndIsPublishedTrue("SKU001"))
                .thenReturn(Optional.of(new Product()));

        // assertThatThrownBy(() -> productService.createProduct(vm))
        //         .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> productService.createProduct(vm))
        .isInstanceOf(DuplicatedException.class);
    }
    @Test
    void getProductById_NotFound() {

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(NotFoundException.class);
    }
    @Test
    void subtractStockQuantity_WhenTrackingDisabled() {

        Product product = new Product();
        product.setId(1L);
        product.setStockTrackingEnabled(false);
        product.setStockQuantity(20L);

        when(productRepository.findAllByIdIn(anyList()))
                .thenReturn(List.of(product));

        ProductQuantityPutVm item = new ProductQuantityPutVm(1L,5L);

        productService.subtractStockQuantity(List.of(item));

        assertThat(product.getStockQuantity()).isEqualTo(20L);

        verify(productRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Tạo sản phẩm thất bại khi brand không tồn tại")
    void createProduct_BrandNotFound() {

        ProductPostVm vm = mock(ProductPostVm.class);

        when(vm.name()).thenReturn("Product A");
        when(vm.slug()).thenReturn("product-a");
        when(vm.sku()).thenReturn("sku-a");
        when(vm.length()).thenReturn(10.0);
        when(vm.width()).thenReturn(5.0);
        when(vm.brandId()).thenReturn(99L);

        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(vm))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Tạo sản phẩm thất bại khi slug đã tồn tại")
    void createProduct_SlugAlreadyExists() {

        ProductPostVm vm = mock(ProductPostVm.class);

        when(vm.slug()).thenReturn("product-a");

        when(productRepository.findBySlugAndIsPublishedTrue("product-a"))
                .thenReturn(Optional.of(new Product()));

        assertThatThrownBy(() -> productService.createProduct(vm))
                .isInstanceOf(DuplicatedException.class)
                .hasMessageContaining("Slug");
    }
    @Test
    void subtractStockQuantity_TrackingDisabled() {

        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(20L);
        product.setStockTrackingEnabled(false);

        when(productRepository.findAllByIdIn(anyList()))
                .thenReturn(List.of(product));

        ProductQuantityPutVm item = new ProductQuantityPutVm(1L, 5L);

        productService.subtractStockQuantity(List.of(item));

        assertThat(product.getStockQuantity()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Xóa sản phẩm thất bại khi không tồn tại")
    void deleteProduct_NotFound() {

        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(10L))
                .isInstanceOf(NotFoundException.class);
    }
    
    @Test
    void getProductById_NoThumbnail() {

        Product product = new Product();
        product.setId(1L);
        product.setThumbnailMediaId(null);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertThat(result.id()).isEqualTo(1L);

        verify(mediaService, never()).getMedia(any());
    }

    // @Test
    // void testServiceIsNotNull() {
    //     assertNotNull(productService);
    // }

    @Test
    void setProductImages_WhenIdsEmpty_ShouldDeleteAllAndReturnEmpty() {
        // Arrange
        Product product = Product.builder().id(1L).build();
        List<Long> emptyIds = Collections.emptyList();

        // Act
        List<ProductImage> result = productService.setProductImages(emptyIds, product);

        // Assert
        assertTrue(result.isEmpty());
        verify(productImageRepository, times(1)).deleteByProductId(1L);
    }

    @Test
    @DisplayName("Update sản phẩm: Lỗi khi slug mới trùng với sản phẩm khác")
    void updateProduct_DuplicateSlug_ThrowsException() {
        // Arrange
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        
        ProductPutVm putVm = mock(ProductPutVm.class);
        when(putVm.slug()).thenReturn("new-slug");

        Product otherProduct = new Product();
        otherProduct.setId(2L); // Sản phẩm khác đã chiếm slug này

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.of(otherProduct));

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, putVm))
                .isInstanceOf(DuplicatedException.class);
    }

    //     // ===============================
    // // Test for ValidateProductPrice
    // // ===============================

    // static class ValidateProductPriceTest {

    //     static class TestClass1 {
    //         @com.yas.product.validation.ValidateProductPrice
    //         Double price;
    //     }

    //     static class TestClass2 {
    //         @com.yas.product.validation.ValidateProductPrice
    //         Double price;
    //     }

    //     static class TestClass3 {
    //         @com.yas.product.validation.ValidateProductPrice
    //         Double price;
    //     }

    //     @Test
    //     void testDefaultMessage() throws Exception {

    //         var annotation = TestClass1.class
    //                 .getDeclaredField("price")
    //                 .getAnnotation(com.yas.product.validation.ValidateProductPrice.class);

    //         assertEquals("Invalid product price", annotation.message());
    //     }

    //     @Test
    //     void testDefaultGroups() throws Exception {

    //         var annotation = TestClass2.class
    //                 .getDeclaredField("price")
    //                 .getAnnotation(com.yas.product.validation.ValidateProductPrice.class);

    //         assertEquals(0, annotation.groups().length);
    //     }

    //     @Test
    //     void testDefaultPayload() throws Exception {

    //         var annotation = TestClass3.class
    //                 .getDeclaredField("price")
    //                 .getAnnotation(com.yas.product.validation.ValidateProductPrice.class);

    //         assertEquals(0, annotation.payload().length);
    //     }
    // }
            
}