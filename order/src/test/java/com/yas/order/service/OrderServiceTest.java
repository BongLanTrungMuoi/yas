package com.yas.order.service;

import static com.yas.order.utils.SecurityContextUtils.setSubjectUpSecurityContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.model.request.OrderRequest;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.order.OrderExistsByProductAndUserGetVm;
import com.yas.order.viewmodel.order.OrderGetVm;
import com.yas.order.viewmodel.order.OrderItemPostVm;
import com.yas.order.viewmodel.order.OrderListVm;
import com.yas.order.viewmodel.order.OrderPostVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.order.PaymentOrderStatusVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private OrderService orderService;

    private OrderAddress shippingAddress;
    private OrderAddress billingAddress;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        shippingAddress = OrderAddress.builder()
            .id(1L)
            .contactName("John Doe")
            .phone("1234567890")
            .addressLine1("123 Main St")
            .city("City")
            .zipCode("12345")
            .districtId(1L)
            .districtName("District")
            .stateOrProvinceId(1L)
            .stateOrProvinceName("State")
            .countryId(1L)
            .countryName("Country")
            .build();

        billingAddress = OrderAddress.builder()
            .id(2L)
            .contactName("John Doe")
            .phone("1234567890")
            .addressLine1("123 Main St")
            .city("City")
            .zipCode("12345")
            .districtId(1L)
            .districtName("District")
            .stateOrProvinceId(1L)
            .stateOrProvinceName("State")
            .countryId(1L)
            .countryName("Country")
            .build();

        sampleOrder = Order.builder()
            .id(1L)
            .email("test@example.com")
            .note("Test note")
            .tax(10.0f)
            .discount(5.0f)
            .numberItem(2)
            .totalPrice(new BigDecimal("100.00"))
            .couponCode("COUPON1")
            .orderStatus(OrderStatus.PENDING)
            .deliveryFee(new BigDecimal("10.00"))
            .deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
            .deliveryStatus(DeliveryStatus.PREPARING)
            .paymentStatus(PaymentStatus.PENDING)
            .shippingAddressId(shippingAddress)
            .billingAddressId(billingAddress)
            .checkoutId("checkout-1")
            .build();
    }

    @Nested
    class CreateOrderTest {

        @Test
        void whenValidOrderPost_shouldCreateOrderAndReturnOrderVm() {
            OrderAddressPostVm billingAddr = OrderAddressPostVm.builder()
                .contactName("John").phone("123").addressLine1("Addr1")
                .city("City").zipCode("12345").districtId(1L).districtName("Dist")
                .stateOrProvinceId(1L).stateOrProvinceName("State")
                .countryId(1L).countryName("Country").build();

            OrderAddressPostVm shippingAddr = OrderAddressPostVm.builder()
                .contactName("Jane").phone("456").addressLine1("Addr2")
                .city("City2").zipCode("67890").districtId(2L).districtName("Dist2")
                .stateOrProvinceId(2L).stateOrProvinceName("State2")
                .countryId(2L).countryName("Country2").build();

            OrderItemPostVm item1 = OrderItemPostVm.builder()
                .productId(100L).productName("Product1").quantity(2)
                .productPrice(new BigDecimal("50.00")).note("item note").build();

            OrderPostVm orderPostVm = OrderPostVm.builder()
                .checkoutId("checkout-1")
                .email("test@example.com")
                .shippingAddressPostVm(shippingAddr)
                .billingAddressPostVm(billingAddr)
                .note("Test")
                .tax(10.0f).discount(5.0f).numberItem(1)
                .totalPrice(new BigDecimal("100.00"))
                .deliveryFee(new BigDecimal("10.00"))
                .couponCode("COUPON")
                .deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
                .paymentStatus(PaymentStatus.PENDING)
                .orderItemPostVms(List.of(item1))
                .build();

            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order o = invocation.getArgument(0);
                o.setId(1L);
                return o;
            });
            when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> {
                Iterable<?> arg = invocation.getArgument(0);
                java.util.List<Object> list = new java.util.ArrayList<>();
                arg.forEach(list::add);
                return list;
            });
            doNothing().when(productService).subtractProductStockQuantity(any());
            doNothing().when(cartService).deleteCartItems(any());
            // acceptOrder stub
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            doNothing().when(promotionService).updateUsagePromotion(anyList());

            OrderVm result = orderService.createOrder(orderPostVm);

            assertNotNull(result);
            verify(orderRepository, atLeastOnce()).save(any(Order.class));
            verify(orderItemRepository).saveAll(any());
            verify(productService).subtractProductStockQuantity(any());
            verify(cartService).deleteCartItems(any());
        }
    }

    @Nested
    class GetOrderWithItemsByIdTest {

        @Test
        void whenOrderExists_shouldReturnOrderVm() {
            OrderItem item = OrderItem.builder()
                .id(1L).productId(100L).productName("Product1")
                .quantity(2).productPrice(new BigDecimal("50.00"))
                .orderId(1L).build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderItemRepository.findAllByOrderId(1L)).thenReturn(List.of(item));

            OrderVm result = orderService.getOrderWithItemsById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("test@example.com", result.email());
        }

        @Test
        void whenOrderNotFound_shouldThrowNotFoundException() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                orderService.getOrderWithItemsById(99L);
            });
        }
    }

    @Nested
    class GetAllOrderTest {

        @Test
        void whenOrdersExist_shouldReturnOrderListVm() {
            sampleOrder.setCreatedOn(ZonedDateTime.now());
            Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(orderPage);

            OrderListVm result = orderService.getAllOrder(
                Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()),
                "", List.of(), Pair.of("", ""), "", Pair.of(0, 10)
            );

            assertNotNull(result);
            assertNotNull(result.orderList());
            assertEquals(1, result.orderList().size());
        }

        @Test
        void whenNoOrders_shouldReturnEmptyOrderList() {
            Page<Order> emptyPage = Page.empty();
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

            OrderListVm result = orderService.getAllOrder(
                Pair.of(ZonedDateTime.now().minusDays(1), ZonedDateTime.now()),
                "", List.of(), Pair.of("", ""), "", Pair.of(0, 10)
            );

            assertNull(result.orderList());
            assertEquals(0, result.totalElements());
        }
    }

    @Nested
    class GetLatestOrdersTest {

        @Test
        void whenCountIsPositive_shouldReturnOrders() {
            sampleOrder.setCreatedOn(ZonedDateTime.now());
            when(orderRepository.getLatestOrders(any(Pageable.class)))
                .thenReturn(List.of(sampleOrder));

            List<OrderBriefVm> result = orderService.getLatestOrders(5);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        void whenCountIsZero_shouldReturnEmptyList() {
            List<OrderBriefVm> result = orderService.getLatestOrders(0);
            assertTrue(result.isEmpty());
        }

        @Test
        void whenCountIsNegative_shouldReturnEmptyList() {
            List<OrderBriefVm> result = orderService.getLatestOrders(-1);
            assertTrue(result.isEmpty());
        }

        @Test
        void whenNoOrdersInDb_shouldReturnEmptyList() {
            when(orderRepository.getLatestOrders(any(Pageable.class)))
                .thenReturn(Collections.emptyList());

            List<OrderBriefVm> result = orderService.getLatestOrders(5);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class IsOrderCompletedWithUserIdAndProductIdTest {

        @Test
        void whenOrderExistsWithNoVariations_shouldReturnTrue() {
            setSubjectUpSecurityContext("user-123");
            when(productService.getProductVariations(100L)).thenReturn(Collections.emptyList());
            when(orderRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(sampleOrder));

            OrderExistsByProductAndUserGetVm result =
                orderService.isOrderCompletedWithUserIdAndProductId(100L);

            assertTrue(result.isPresent());
        }

        @Test
        void whenOrderDoesNotExist_shouldReturnFalse() {
            setSubjectUpSecurityContext("user-123");
            when(productService.getProductVariations(100L)).thenReturn(Collections.emptyList());
            when(orderRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

            OrderExistsByProductAndUserGetVm result =
                orderService.isOrderCompletedWithUserIdAndProductId(100L);

            assertFalse(result.isPresent());
        }

        @Test
        void whenProductHasVariations_shouldUseVariationIds() {
            setSubjectUpSecurityContext("user-123");
            List<ProductVariationVm> variations = List.of(
                new ProductVariationVm(200L, "Var1", "SKU1"),
                new ProductVariationVm(201L, "Var2", "SKU2")
            );
            when(productService.getProductVariations(100L)).thenReturn(variations);
            when(orderRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(sampleOrder));

            OrderExistsByProductAndUserGetVm result =
                orderService.isOrderCompletedWithUserIdAndProductId(100L);

            assertTrue(result.isPresent());
        }
    }

    @Nested
    class GetMyOrdersTest {

        @Test
        void whenUserHasOrders_shouldReturnOrderList() {
            setSubjectUpSecurityContext("user-123");
            sampleOrder.setCreatedOn(ZonedDateTime.now());
            when(orderRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(sampleOrder));

            List<OrderGetVm> result = orderService.getMyOrders("", OrderStatus.PENDING);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        void whenUserHasNoOrders_shouldReturnEmptyList() {
            setSubjectUpSecurityContext("user-123");
            when(orderRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(Collections.emptyList());

            List<OrderGetVm> result = orderService.getMyOrders("", OrderStatus.PENDING);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FindOrderByCheckoutIdTest {

        @Test
        void whenOrderExists_shouldReturnOrder() {
            when(orderRepository.findByCheckoutId("checkout-1"))
                .thenReturn(Optional.of(sampleOrder));

            Order result = orderService.findOrderByCheckoutId("checkout-1");

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        void whenOrderNotFound_shouldThrowNotFoundException() {
            when(orderRepository.findByCheckoutId("not-found"))
                .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                orderService.findOrderByCheckoutId("not-found");
            });
        }
    }

    @Nested
    class FindOrderVmByCheckoutIdTest {

        @Test
        void whenOrderExists_shouldReturnOrderGetVm() {
            OrderItem item = OrderItem.builder()
                .id(1L).productId(100L).productName("Product1")
                .quantity(1).productPrice(new BigDecimal("50.00"))
                .orderId(1L).build();

            when(orderRepository.findByCheckoutId("checkout-1"))
                .thenReturn(Optional.of(sampleOrder));
            when(orderItemRepository.findAllByOrderId(1L))
                .thenReturn(List.of(item));

            OrderGetVm result = orderService.findOrderVmByCheckoutId("checkout-1");

            assertNotNull(result);
            assertEquals(1L, result.id());
        }
    }

    @Nested
    class UpdateOrderPaymentStatusTest {

        @Test
        void whenOrderExists_shouldUpdatePaymentStatus() {
            PaymentOrderStatusVm statusVm = PaymentOrderStatusVm.builder()
                .orderId(1L)
                .paymentId(999L)
                .paymentStatus("COMPLETED")
                .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

            PaymentOrderStatusVm result = orderService.updateOrderPaymentStatus(statusVm);

            assertNotNull(result);
            assertEquals(1L, result.orderId());
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        void whenOrderNotFound_shouldThrowNotFoundException() {
            PaymentOrderStatusVm statusVm = PaymentOrderStatusVm.builder()
                .orderId(99L)
                .paymentId(999L)
                .paymentStatus("COMPLETED")
                .build();

            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                orderService.updateOrderPaymentStatus(statusVm);
            });
        }

        @Test
        void whenPaymentCompleted_shouldSetOrderStatusToPaid() {
            PaymentOrderStatusVm statusVm = PaymentOrderStatusVm.builder()
                .orderId(1L)
                .paymentId(999L)
                .paymentStatus("COMPLETED")
                .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderPaymentStatus(statusVm);

            assertEquals(OrderStatus.PAID, sampleOrder.getOrderStatus());
            assertEquals(PaymentStatus.COMPLETED, sampleOrder.getPaymentStatus());
        }
    }

    @Nested
    class RejectOrderTest {

        @Test
        void whenOrderExists_shouldRejectOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

            orderService.rejectOrder(1L, "Out of stock");

            assertEquals(OrderStatus.REJECT, sampleOrder.getOrderStatus());
            assertEquals("Out of stock", sampleOrder.getRejectReason());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        void whenOrderNotFound_shouldThrowNotFoundException() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                orderService.rejectOrder(99L, "reason");
            });
        }
    }

    @Nested
    class AcceptOrderTest {

        @Test
        void whenOrderExists_shouldAcceptOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

            orderService.acceptOrder(1L);

            assertEquals(OrderStatus.ACCEPTED, sampleOrder.getOrderStatus());
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        void whenOrderNotFound_shouldThrowNotFoundException() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                orderService.acceptOrder(99L);
            });
        }
    }

    @Nested
    class ExportCsvTest {

        @Test
        void whenNoOrders_shouldReturnEmptyCsv() throws Exception {
            OrderRequest request = OrderRequest.builder()
                .createdFrom(ZonedDateTime.now().minusDays(1))
                .createdTo(ZonedDateTime.now())
                .productName("")
                .orderStatus(List.of())
                .billingCountry("")
                .billingPhoneNumber("")
                .email("")
                .pageNo(0)
                .pageSize(10)
                .build();

            Page<Order> emptyPage = Page.empty();
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

            byte[] result = orderService.exportCsv(request);

            assertNotNull(result);
        }

        @Test
        void whenOrdersExist_shouldReturnCsvBytes() throws Exception {
            sampleOrder.setCreatedOn(ZonedDateTime.now());

            OrderRequest request = OrderRequest.builder()
                .createdFrom(ZonedDateTime.now().minusDays(1))
                .createdTo(ZonedDateTime.now())
                .productName("")
                .orderStatus(List.of())
                .billingCountry("")
                .billingPhoneNumber("")
                .email("")
                .pageNo(0)
                .pageSize(10)
                .build();

            Page<Order> orderPage = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(orderPage);

            OrderItemCsv csv = OrderItemCsv.builder()
                .id(1L)
                .email("test@example.com")
                .orderStatus(OrderStatus.PENDING)
                .build();
            when(orderMapper.toCsv(any(OrderBriefVm.class))).thenReturn(csv);

            byte[] result = orderService.exportCsv(request);

            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }
}
