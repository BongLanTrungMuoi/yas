package com.yas.order.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapperImpl();
    }

    @Test
    void toCsv_whenValidOrderBriefVm_shouldMapAllFields() {
        OrderAddressVm billingAddress = OrderAddressVm.builder()
            .id(1L)
            .contactName("John Doe")
            .phone("1234567890")
            .addressLine1("123 Main St")
            .city("City")
            .zipCode("12345")
            .countryId(1L)
            .countryName("Country")
            .build();

        ZonedDateTime createdOn = ZonedDateTime.now();

        OrderBriefVm orderBriefVm = OrderBriefVm.builder()
            .id(1L)
            .email("test@example.com")
            .billingAddressVm(billingAddress)
            .totalPrice(new BigDecimal("100.00"))
            .orderStatus(OrderStatus.PENDING)
            .deliveryMethod(DeliveryMethod.GRAB_EXPRESS)
            .deliveryStatus(DeliveryStatus.PREPARING)
            .paymentStatus(PaymentStatus.PENDING)
            .createdOn(createdOn)
            .build();

        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(new BigDecimal("100.00"), result.getTotalPrice());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(DeliveryStatus.PREPARING, result.getDeliveryStatus());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(createdOn, result.getCreatedOn());
    }

    @Test
    void toCsv_whenNullInput_shouldReturnNull() {
        OrderItemCsv result = orderMapper.toCsv(null);
        assertNull(result);
    }

    @Test
    void toCsv_whenNullBillingAddress_shouldMapPhoneAsNull() {
        OrderBriefVm orderBriefVm = OrderBriefVm.builder()
            .id(2L)
            .email("test2@example.com")
            .billingAddressVm(null)
            .totalPrice(new BigDecimal("200.00"))
            .orderStatus(OrderStatus.COMPLETED)
            .deliveryStatus(DeliveryStatus.DELIVERED)
            .paymentStatus(PaymentStatus.COMPLETED)
            .createdOn(ZonedDateTime.now())
            .build();

        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertNull(result.getPhone());
        assertEquals("test2@example.com", result.getEmail());
    }

    @Test
    void toCsv_shouldMapIdFromOrderBriefVm() {
        OrderBriefVm orderBriefVm = OrderBriefVm.builder()
            .id(42L)
            .email("user@example.com")
            .billingAddressVm(null)
            .totalPrice(new BigDecimal("50.00"))
            .orderStatus(OrderStatus.ACCEPTED)
            .build();

        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        assertNotNull(result);
        assertEquals(42L, result.getId());
    }

    @Test
    void toCsv_shouldMapPhoneFromBillingAddress() {
        OrderAddressVm billingAddress = OrderAddressVm.builder()
            .phone("9876543210")
            .build();

        OrderBriefVm orderBriefVm = OrderBriefVm.builder()
            .id(3L)
            .billingAddressVm(billingAddress)
            .orderStatus(OrderStatus.SHIPPING)
            .build();

        OrderItemCsv result = orderMapper.toCsv(orderBriefVm);

        assertNotNull(result);
        assertEquals("9876543210", result.getPhone());
    }
}
