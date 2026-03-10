package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    private ProductDetailService productDetailService;

    @BeforeEach
    void setUp() {
        productDetailService = new ProductDetailService(
            productRepository, mediaService, productOptionCombinationRepository
        );
    }

    @Nested
    class GetProductDetailByIdTest {

        @Test
        void whenProductNotFound_shouldThrowNotFoundException() {
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                productDetailService.getProductDetailById(1L);
            });
        }

        @Test
        void whenProductNotPublished_shouldThrowNotFoundException() {
            Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .isPublished(false)
                .build();
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(NotFoundException.class, () -> {
                productDetailService.getProductDetailById(1L);
            });
        }

        @Test
        void whenProductFound_withNoBrand_shouldReturnDetailWithNullBrand() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Product", result.getName());
            assertNull(result.getBrandId());
            assertNull(result.getBrandName());
        }

        @Test
        void whenProductFound_withBrand_shouldReturnDetailWithBrand() {
            Product product = createBasicProduct();
            Brand brand = new Brand();
            brand.setId(10L);
            brand.setName("Test Brand");
            product.setBrand(brand);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(10L, result.getBrandId());
            assertEquals("Test Brand", result.getBrandName());
        }

        @Test
        void whenProductFound_withCategories_shouldReturnDetailWithCategories() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setHasOptions(false);
            product.setAttributeValues(Collections.emptyList());

            Category category = new Category();
            category.setId(5L);
            category.setName("Category1");
            ProductCategory pc = ProductCategory.builder()
                .product(product)
                .category(category)
                .build();
            product.setProductCategories(List.of(pc));

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(1, result.getCategories().size());
            assertEquals("Category1", result.getCategories().get(0).getName());
        }

        @Test
        void whenProductFound_withNullCategories_shouldReturnEmptyCategories() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setHasOptions(false);
            product.setAttributeValues(Collections.emptyList());
            product.setProductCategories(null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertTrue(result.getCategories().isEmpty());
        }

        @Test
        void whenProductFound_withAttributeValues_shouldReturnDetailWithAttributes() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setHasOptions(false);

            ProductAttribute attribute = new ProductAttribute();
            attribute.setId(1L);
            attribute.setName("Color");

            ProductAttributeValue attrValue = new ProductAttributeValue();
            attrValue.setId(1L);
            attrValue.setProductAttribute(attribute);
            attrValue.setValue("Red");
            product.setAttributeValues(List.of(attrValue));

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(1, result.getAttributeValues().size());
            assertEquals("Color", result.getAttributeValues().get(0).nameProductAttribute());
            assertEquals("Red", result.getAttributeValues().get(0).value());
        }

        @Test
        void whenProductFound_withThumbnail_shouldReturnDetailWithThumbnail() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);
            product.setThumbnailMediaId(100L);

            NoFileMediaVm mediaVm = new NoFileMediaVm(100L, "thumb", "thumb.jpg", "image/jpeg",
                "http://example.com/thumb.jpg");
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(mediaService.getMedia(100L)).thenReturn(mediaVm);

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertNotNull(result.getThumbnail());
            assertEquals(100L, result.getThumbnail().id());
            assertEquals("http://example.com/thumb.jpg", result.getThumbnail().url());
        }

        @Test
        void whenProductFound_withNoThumbnail_shouldReturnNullThumbnail() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);
            product.setThumbnailMediaId(null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertNull(result.getThumbnail());
        }

        @Test
        void whenProductFound_withProductImages_shouldReturnDetailWithImages() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);
            product.setThumbnailMediaId(null);

            ProductImage image1 = ProductImage.builder().id(1L).imageId(200L).product(product).build();
            ProductImage image2 = ProductImage.builder().id(2L).imageId(201L).product(product).build();
            product.setProductImages(List.of(image1, image2));

            NoFileMediaVm mediaVm1 = new NoFileMediaVm(200L, "img1", "img1.jpg", "image/jpeg",
                "http://example.com/img1.jpg");
            NoFileMediaVm mediaVm2 = new NoFileMediaVm(201L, "img2", "img2.jpg", "image/jpeg",
                "http://example.com/img2.jpg");

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(mediaService.getMedia(200L)).thenReturn(mediaVm1);
            when(mediaService.getMedia(201L)).thenReturn(mediaVm2);

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(2, result.getProductImages().size());
            assertEquals("http://example.com/img1.jpg", result.getProductImages().get(0).url());
            assertEquals("http://example.com/img2.jpg", result.getProductImages().get(1).url());
        }

        @Test
        void whenProductFound_withVariations_shouldReturnDetailWithVariations() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(true);
            product.setThumbnailMediaId(null);
            product.setProductImages(null);

            // Create variation product
            Product variation = Product.builder()
                .id(2L)
                .name("Variation1")
                .slug("variation-1")
                .sku("SKU-V1")
                .gtin("GTIN-V1")
                .price(19.99)
                .isPublished(true)
                .thumbnailMediaId(null)
                .productImages(Collections.emptyList())
                .build();

            product.setProducts(List.of(variation));

            ProductOption option = new ProductOption();
            option.setId(1L);
            option.setName("Size");

            ProductOptionCombination combo = ProductOptionCombination.builder()
                .id(1L)
                .product(variation)
                .productOption(option)
                .value("Large")
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals(1, result.getVariations().size());
            assertEquals("Variation1", result.getVariations().get(0).name());
            assertEquals("Large", result.getVariations().get(0).options().get(1L));
        }

        @Test
        void whenProductFound_withUnpublishedVariation_shouldFilterOut() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(true);
            product.setThumbnailMediaId(null);
            product.setProductImages(null);

            Product publishedVariation = Product.builder()
                .id(2L)
                .name("PublishedVariation")
                .slug("pub-var")
                .sku("SKU1")
                .gtin("GTIN1")
                .price(10.0)
                .isPublished(true)
                .thumbnailMediaId(null)
                .productImages(Collections.emptyList())
                .build();

            Product unpublishedVariation = Product.builder()
                .id(3L)
                .name("UnpublishedVariation")
                .slug("unpub-var")
                .sku("SKU2")
                .gtin("GTIN2")
                .price(15.0)
                .isPublished(false)
                .thumbnailMediaId(null)
                .productImages(Collections.emptyList())
                .build();

            product.setProducts(List.of(publishedVariation, unpublishedVariation));

            ProductOption option = new ProductOption();
            option.setId(1L);
            option.setName("Color");

            ProductOptionCombination combo = ProductOptionCombination.builder()
                .id(1L)
                .product(publishedVariation)
                .productOption(option)
                .value("Red")
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productOptionCombinationRepository.findAllByProduct(publishedVariation))
                .thenReturn(List.of(combo));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            // Only published variation should be present
            assertEquals(1, result.getVariations().size());
            assertEquals("PublishedVariation", result.getVariations().get(0).name());
        }

        @Test
        void whenProductFound_withNoOptions_shouldReturnEmptyVariations() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);
            product.setThumbnailMediaId(null);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertTrue(result.getVariations().isEmpty());
        }

        @Test
        void whenProductFound_shouldReturnAllBasicFields() {
            Product product = createBasicProduct();
            product.setBrand(null);
            product.setProductCategories(Collections.emptyList());
            product.setAttributeValues(Collections.emptyList());
            product.setHasOptions(false);
            product.setThumbnailMediaId(null);
            product.setShortDescription("Short desc");
            product.setDescription("Full description");
            product.setSpecification("Specification");
            product.setSku("SKU-001");
            product.setGtin("GTIN-001");
            product.setSlug("test-product");
            product.setAllowedToOrder(true);
            product.setFeatured(true);
            product.setVisibleIndividually(true);
            product.setStockTrackingEnabled(true);
            product.setPrice(29.99);
            product.setMetaTitle("Meta Title");
            product.setMetaKeyword("keyword");
            product.setMetaDescription("Meta Description");
            product.setTaxClassId(5L);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

            assertNotNull(result);
            assertEquals("Test Product", result.getName());
            assertEquals("Short desc", result.getShortDescription());
            assertEquals("Full description", result.getDescription());
            assertEquals("Specification", result.getSpecification());
            assertEquals("SKU-001", result.getSku());
            assertEquals("GTIN-001", result.getGtin());
            assertEquals("test-product", result.getSlug());
            assertTrue(result.getIsAllowedToOrder());
            assertTrue(result.getIsPublished());
            assertTrue(result.getIsFeatured());
            assertTrue(result.getIsVisible());
            assertTrue(result.getStockTrackingEnabled());
            assertEquals(29.99, result.getPrice());
            assertEquals("Meta Title", result.getMetaTitle());
            assertEquals("keyword", result.getMetaKeyword());
            assertEquals("Meta Description", result.getMetaDescription());
            assertEquals(5L, result.getTaxClassId());
        }
    }

    private Product createBasicProduct() {
        return Product.builder()
            .id(1L)
            .name("Test Product")
            .isPublished(true)
            .productImages(Collections.emptyList())
            .productCategories(new ArrayList<>())
            .attributeValues(new ArrayList<>())
            .products(new ArrayList<>())
            .build();
    }
}
