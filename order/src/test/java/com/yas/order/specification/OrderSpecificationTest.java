package com.yas.order.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.order.model.Order;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class OrderSpecificationTest {

    private CriteriaBuilder criteriaBuilder;
    private Root<Order> root;
    private CriteriaQuery<?> query;
    private Root<OrderItem> orderItemRoot;

    @BeforeEach
    void setUp() {
        criteriaBuilder = mock(CriteriaBuilder.class);
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        orderItemRoot = mock(Root.class);
        when(criteriaBuilder.conjunction()).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
    }

    @Test
    void testHasCreatedBy_whenNormalCase_thenSuccess() {

        String createdBy = "user123";

        when(root.get("createdBy")).thenReturn(mock(Path.class));

        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(root.get("createdBy"), createdBy)).thenReturn(expectedPredicate);

        Specification<Order> spec = OrderSpecification.hasCreatedBy(createdBy);
        Predicate resultPredicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(resultPredicate, "Predicate should not be null");
        assertEquals(expectedPredicate, resultPredicate, "Predicate should match expected predicate");
    }

    @Test
    void testHasOrderStatus_whenNormalCase_thenSuccess() {
        when(root.get("orderStatus")).thenReturn(mock(Path.class));
        Predicate expectedPredicate = mock(Predicate.class);
        when(criteriaBuilder.equal(root.get("orderStatus"), OrderStatus.COMPLETED)).thenReturn(expectedPredicate);

        Specification<Order> spec = OrderSpecification.hasOrderStatus(OrderStatus.COMPLETED);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        assertEquals(expectedPredicate, predicate);
    }

    @Test
    void testHasProductNameInOrderItems_whenNormalCase_thenSuccess() {

        Subquery<Long> subqueryMock = mock(Subquery.class);
        when(query.subquery(Long.class)).thenReturn(subqueryMock);

        when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
        when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

        Subquery subquery1 = mock(Subquery.class);
        when(subqueryMock.select(any())).thenReturn(subquery1);

        Subquery subquery2 = mock(Subquery.class);
        when(subquery1.where(any(Predicate.class))).thenReturn(subquery2);

        CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
        when(criteriaBuilder.in(any())).thenReturn(inMock);
        when(inMock.value(any())).thenReturn(mock(CriteriaBuilder.In.class));

        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("SampleProduct");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithEmail_whenNormalCase_thenSuccess() {
        when(root.get("email")).thenReturn(mock(Path.class));
        when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withEmail("test@example.com");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithOrderStatusList_whenNormalCase_thenSuccess() {
        Path pathMock = mock(Path.class);
        when(root.get("orderStatus")).thenReturn(pathMock);
        when(pathMock.in(any(Collection.class))).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withOrderStatus(List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithBillingPhoneNumber_whenNormalCase_thenSuccess() {

        Path pathMock = mock(Path.class);
        when(root.get("billingAddressId")).thenReturn(pathMock);
        when(pathMock.get("phone")).thenReturn(mock(Path.class));
        when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber("1234567890");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithCountryName_whenNormalCase_thenSuccess() {

        Path path = mock(Path.class);
        when(root.get("billingAddressId")).thenReturn(path);
        when(path.get("countryName")).thenReturn(mock(Path.class));
        when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withCountryName("USA");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithDateRange_whenNormalCase_thenSuccess() {
        ZonedDateTime createdFrom = ZonedDateTime.now().minusDays(7);
        ZonedDateTime createdTo = ZonedDateTime.now();
        when(root.get("createdOn")).thenReturn(mock(Path.class));
        when(criteriaBuilder.between(root.get("createdOn"), createdFrom, createdTo)).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withDateRange(createdFrom, createdTo);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    // --- Null/empty branch tests ---

    @Test
    void testHasOrderStatus_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.hasOrderStatus(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testHasProductNameInOrderItems_whenQueryNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("SomeProduct");
        Predicate predicate = spec.toPredicate(root, null, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testHasProductNameInOrderItems_whenProductNameEmpty_thenNoLikePredicate() {
        Subquery<Long> subqueryMock = mock(Subquery.class);
        when(query.subquery(Long.class)).thenReturn(subqueryMock);
        when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);

        Subquery subquery1 = mock(Subquery.class);
        when(subqueryMock.select(any())).thenReturn(subquery1);
        when(subquery1.where(any(Predicate.class))).thenReturn(mock(Subquery.class));

        CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
        when(criteriaBuilder.in(any())).thenReturn(inMock);
        when(inMock.value(any())).thenReturn(mock(CriteriaBuilder.In.class));

        Specification<Order> spec = OrderSpecification.hasProductNameInOrderItems("");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    void testWithEmail_whenEmpty_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withEmail("");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithEmail_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withEmail(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithOrderStatusList_whenEmpty_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withOrderStatus(List.of());
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithOrderStatusList_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withOrderStatus((List<OrderStatus>) null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithBillingPhoneNumber_whenEmpty_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber("");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithBillingPhoneNumber_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withBillingPhoneNumber(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithCountryName_whenEmpty_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withCountryName("");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithCountryName_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withCountryName(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithDateRange_whenFromNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withDateRange(null, ZonedDateTime.now());
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithDateRange_whenToNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withDateRange(ZonedDateTime.now(), null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithDateRange_whenBothNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withDateRange(null, null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    // --- withProductName tests ---

    @Test
    void testWithProductName_whenNormalCase_thenSuccess() {
        Subquery<Long> subqueryMock = mock(Subquery.class);
        when(query.subquery(Long.class)).thenReturn(subqueryMock);
        when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
        when(subqueryMock.select(any())).thenReturn(subqueryMock);

        when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.exists(any())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.withProductName("Product");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testWithProductName_whenQueryNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withProductName("Product");
        Predicate predicate = spec.toPredicate(root, null, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithProductName_whenEmpty_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withProductName("");
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testWithProductName_whenNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.withProductName(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    // --- hasProductInOrderItems tests ---

    @Test
    void testHasProductInOrderItems_whenNormalCase_thenSuccess() {
        Subquery<OrderItem> subqueryMock = mock(Subquery.class);
        when(query.subquery(OrderItem.class)).thenReturn(subqueryMock);
        when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
        when(subqueryMock.select(any())).thenReturn(subqueryMock);

        when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));
        Path pathMock = mock(Path.class);
        when(orderItemRoot.get("productId")).thenReturn(pathMock);
        CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
        when(pathMock.in(any(List.class))).thenReturn(inMock);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));
        when(subqueryMock.where(any(Predicate.class))).thenReturn(subqueryMock);
        when(criteriaBuilder.exists(any())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.hasProductInOrderItems(List.of(1L, 2L));
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    @Test
    void testHasProductInOrderItems_whenQueryNull_thenReturnsConjunction() {
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);

        Specification<Order> spec = OrderSpecification.hasProductInOrderItems(List.of(1L));
        Predicate predicate = spec.toPredicate(root, null, criteriaBuilder);

        assertEquals(conjunction, predicate);
    }

    @Test
    void testHasProductInOrderItems_whenProductIdsNull_thenUsesEmptyList() {
        Subquery<OrderItem> subqueryMock = mock(Subquery.class);
        when(query.subquery(OrderItem.class)).thenReturn(subqueryMock);
        when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
        when(subqueryMock.select(any())).thenReturn(subqueryMock);

        when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));
        Path pathMock = mock(Path.class);
        when(orderItemRoot.get("productId")).thenReturn(pathMock);
        CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
        when(pathMock.in(any(List.class))).thenReturn(inMock);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));
        when(subqueryMock.where(any(Predicate.class))).thenReturn(subqueryMock);
        when(criteriaBuilder.exists(any())).thenReturn(mock(Predicate.class));

        Specification<Order> spec = OrderSpecification.hasProductInOrderItems(null);
        Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

        assertNotNull(predicate);
    }

    // --- Composite method tests ---

    @Nested
    class FindMyOrdersTest {
        @Test
        void testFindMyOrders_whenAllParams_thenSuccess() {
            // Setup for hasCreatedBy
            when(root.get("createdBy")).thenReturn(mock(Path.class));
            when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));

            // Setup for hasOrderStatus
            when(root.get("orderStatus")).thenReturn(mock(Path.class));

            // Setup for hasProductNameInOrderItems (subquery)
            Subquery<Long> subqueryMock = mock(Subquery.class);
            when(query.subquery(Long.class)).thenReturn(subqueryMock);
            when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
            Subquery subquery1 = mock(Subquery.class);
            when(subqueryMock.select(any())).thenReturn(subquery1);
            when(subquery1.where(any(Predicate.class))).thenReturn(mock(Subquery.class));
            when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

            CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
            when(criteriaBuilder.in(any())).thenReturn(inMock);
            when(inMock.value(any())).thenReturn(mock(CriteriaBuilder.In.class));

            Specification<Order> spec = OrderSpecification.findMyOrders(
                "user1", "Product", OrderStatus.COMPLETED);
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(predicate);
        }

        @Test
        void testFindMyOrders_whenNullStatusAndProduct_thenSuccess() {
            when(root.get("createdBy")).thenReturn(mock(Path.class));
            when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));
            when(root.get("orderStatus")).thenReturn(mock(Path.class));

            // hasProductNameInOrderItems with null query path won't happen here since query is not null
            Subquery<Long> subqueryMock = mock(Subquery.class);
            when(query.subquery(Long.class)).thenReturn(subqueryMock);
            when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
            Subquery subquery1 = mock(Subquery.class);
            when(subqueryMock.select(any())).thenReturn(subquery1);
            when(subquery1.where(any(Predicate.class))).thenReturn(mock(Subquery.class));

            CriteriaBuilder.In inMock = mock(CriteriaBuilder.In.class);
            when(criteriaBuilder.in(any())).thenReturn(inMock);
            when(inMock.value(any())).thenReturn(mock(CriteriaBuilder.In.class));

            Specification<Order> spec = OrderSpecification.findMyOrders("user1", null, null);
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(predicate);
        }
    }

    @Nested
    class FindOrderByWithMulCriteriaTest {

        @Test
        void testFindOrderByWithMulCriteria_whenAllParams_thenSuccess() {
            // query.getResultType() returns something other than Long.class -> triggers fetch
            when(query.getResultType()).thenReturn((Class) Order.class);
            when(root.fetch("shippingAddressId", JoinType.LEFT)).thenReturn(mock(Fetch.class));
            when(root.fetch("billingAddressId", JoinType.LEFT)).thenReturn(mock(Fetch.class));

            // withEmail
            when(root.get("email")).thenReturn(mock(Path.class));
            when(criteriaBuilder.like(any(), anyString())).thenReturn(mock(Predicate.class));

            // withOrderStatus
            Path statusPath = mock(Path.class);
            when(root.get("orderStatus")).thenReturn(statusPath);
            when(statusPath.in(any(Collection.class))).thenReturn(mock(Predicate.class));

            // withProductName
            Subquery<Long> subqueryMock = mock(Subquery.class);
            when(query.subquery(Long.class)).thenReturn(subqueryMock);
            when(subqueryMock.from(OrderItem.class)).thenReturn(orderItemRoot);
            when(subqueryMock.select(any())).thenReturn(subqueryMock);
            when(criteriaBuilder.equal(any(), any())).thenReturn(mock(Predicate.class));
            when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));
            when(criteriaBuilder.exists(any())).thenReturn(mock(Predicate.class));

            // withBillingPhoneNumber
            Path billingPath = mock(Path.class);
            when(root.get("billingAddressId")).thenReturn(billingPath);
            when(billingPath.get("phone")).thenReturn(mock(Path.class));
            when(billingPath.get("countryName")).thenReturn(mock(Path.class));

            // withDateRange
            ZonedDateTime from = ZonedDateTime.now().minusDays(7);
            ZonedDateTime to = ZonedDateTime.now();
            when(root.get("createdOn")).thenReturn(mock(Path.class));
            when(criteriaBuilder.between(any(), any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(mock(Predicate.class));

            Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
                List.of(OrderStatus.COMPLETED),
                "1234567890",
                "USA",
                "test@test.com",
                "ProductName",
                from, to
            );
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(predicate);
            verify(root).fetch("shippingAddressId", JoinType.LEFT);
            verify(root).fetch("billingAddressId", JoinType.LEFT);
        }

        @Test
        void testFindOrderByWithMulCriteria_whenResultTypeIsLong_thenNoFetch() {
            // query.getResultType() returns Long.class -> no fetch
            when(query.getResultType()).thenReturn((Class) Long.class);

            // Minimal setup for internal specs to return conjunction
            when(root.get("email")).thenReturn(mock(Path.class));
            when(root.get("orderStatus")).thenReturn(mock(Path.class));
            when(root.get("createdOn")).thenReturn(mock(Path.class));
            when(root.get("billingAddressId")).thenReturn(mock(Path.class));

            Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
                null, null, null, null, null, null, null);
            Predicate predicate = spec.toPredicate(root, query, criteriaBuilder);

            assertNotNull(predicate);
            verify(root, never()).fetch(anyString(), any(JoinType.class));
        }

        @Test
        void testFindOrderByWithMulCriteria_whenQueryNull_thenNoFetchAndConjunction() {
            // query is null => null check in findOrderByWithMulCriteria
            // Actually query null does not enter the if block
            // But inner methods like withProductName will handle null query
            Specification<Order> spec = OrderSpecification.findOrderByWithMulCriteria(
                null, null, null, null, null, null, null);
            Predicate predicate = spec.toPredicate(root, null, criteriaBuilder);

            assertNotNull(predicate);
        }
    }

    @Nested
    class ExistsByCreatedByAndInProductIdAndOrderStatusCompletedTest {
        @Test
        void testExistsByCreatedBy_whenNormalCase_thenReturnsSpecification() {
            Specification<Order> spec =
                OrderSpecification.existsByCreatedByAndInProductIdAndOrderStatusCompleted(
                    "user1", List.of(1L, 2L));

            assertNotNull(spec);
        }
    }
}
