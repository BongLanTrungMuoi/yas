package com.yas.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.config.ServiceUrlConfig;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import com.yas.order.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private ServiceUrlConfig serviceUrlConfig;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(restClient, serviceUrlConfig);
        when(serviceUrlConfig.product()).thenReturn("http://product-service");

        // Set up security context
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("test-token");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    class GetProductVariationsTest {

        @Test
        void whenCalled_shouldReturnVariationList() {
            List<ProductVariationVm> expected = List.of(
                new ProductVariationVm(1L, "Var1", "SKU1"),
                new ProductVariationVm(2L, "Var2", "SKU2")
            );

            ResponseEntity<List<ProductVariationVm>> responseEntity = ResponseEntity.ok(expected);

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

            List<ProductVariationVm> result = productService.getProductVariations(100L);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Var1", result.get(0).name());
        }
    }

    @Nested
    class GetProductInfomationTest {

        @Test
        void whenProductsExist_shouldReturnProductMap() {
            ProductCheckoutListVm product1 = ProductCheckoutListVm.builder()
                .id(1L).name("Product1").price(10.0).build();
            ProductCheckoutListVm product2 = ProductCheckoutListVm.builder()
                .id(2L).name("Product2").price(20.0).build();

            ProductGetCheckoutListVm response = new ProductGetCheckoutListVm(
                List.of(product1, product2), 0, 10, 2, 1, true
            );

            ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(response);

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

            Map<Long, ProductCheckoutListVm> result =
                productService.getProductInfomation(Set.of(1L, 2L), 0, 10);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey(1L));
            assertTrue(result.containsKey(2L));
        }

        @Test
        void whenResponseIsNull_shouldThrowNotFoundException() {
            ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(null);

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

            assertThrows(NotFoundException.class, () -> {
                productService.getProductInfomation(Set.of(1L), 0, 10);
            });
        }

        @Test
        void whenProductListIsNull_shouldThrowNotFoundException() {
            ProductGetCheckoutListVm response = new ProductGetCheckoutListVm(
                null, 0, 10, 0, 0, true
            );
            ResponseEntity<ProductGetCheckoutListVm> responseEntity = ResponseEntity.ok(response);

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

            assertThrows(NotFoundException.class, () -> {
                productService.getProductInfomation(Set.of(1L), 0, 10);
            });
        }
    }

    @Nested
    class SubtractProductStockQuantityTest {

        @Test
        void whenCalled_shouldCallPutEndpoint() {
            OrderItemVm item = OrderItemVm.builder()
                .id(1L).productId(100L).productName("Product1")
                .quantity(2).productPrice(new BigDecimal("50.00"))
                .orderId(1L).build();

            OrderVm orderVm = OrderVm.builder()
                .id(1L).orderItemVms(Set.of(item))
                .orderStatus(OrderStatus.PENDING).build();

            // Use RETURNS_SELF for the chained body() call
            RestClient.RequestBodySpec deepSpec = mock(RestClient.RequestBodySpec.class,
                org.mockito.Mockito.RETURNS_SELF);

            when(restClient.put()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(deepSpec);
            when(deepSpec.retrieve()).thenReturn(responseSpec);

            productService.subtractProductStockQuantity(orderVm);

            verify(restClient).put();
        }
    }

    @Nested
    class FallbackHandlerTest {

        @Test
        void handleProductVariationListFallback_shouldThrowOriginalException() {
            RuntimeException exception = new RuntimeException("Connection refused");

            assertThrows(RuntimeException.class, () -> {
                productService.handleProductVariationListFallback(exception);
            });
        }

        @Test
        void handleProductInfomationFallback_shouldThrowOriginalException() {
            RuntimeException exception = new RuntimeException("Service unavailable");

            assertThrows(RuntimeException.class, () -> {
                productService.handleProductInfomationFallback(exception);
            });
        }
    }
}
