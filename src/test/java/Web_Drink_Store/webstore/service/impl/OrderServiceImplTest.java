package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.order.OrderRequest;
import Web_Drink_Store.webstore.dto.order.OrderResponse;
import Web_Drink_Store.webstore.entity.*;
import Web_Drink_Store.webstore.enums.DiscountType;
import Web_Drink_Store.webstore.enums.OrderStatus;
import Web_Drink_Store.webstore.enums.ProductStatus;
import Web_Drink_Store.webstore.enums.PromotionStatus;
import Web_Drink_Store.webstore.exception.BadRequestException;
import Web_Drink_Store.webstore.exception.ResourceNotFoundException;
import Web_Drink_Store.webstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orders;
    @Mock
    private OrderItemRepository orderItems;
    @Mock
    private UserRepository users;
    @Mock
    private CartRepository carts;
    @Mock
    private CartItemRepository cartItems;
    @Mock
    private ProductRepository products;
    @Mock
    private PromotionRepository promotions;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Cart testCart;
    private OrderRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User();
        // Giả lập user id = 1 qua reflection hoặc subclass nếu id không có setter
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, 1L);
        } catch (Exception ignored) {}
        testUser.setFullName("Nguyen Van A");
        testUser.setEmail("vana@example.com");

        testCart = new Cart();
        testCart.setUser(testUser);

        request = new OrderRequest();
        request.setReceiverName("Nguyen Van A");
        request.setReceiverPhone("0901234567");
        request.setShippingAddress("123 Nguyen Hue, Q1, HCM");

        lenient().when(users.findById(1L)).thenReturn(Optional.of(testUser));
        lenient().when(carts.findByUserId(1L)).thenReturn(Optional.of(testCart));
        lenient().when(orderItems.findByOrderId(any())).thenReturn(new ArrayList<>());
    }

    private CartItem createCartItem(BigDecimal unitPrice, int quantity) {
        Product product = new Product();
        product.setName("Tra Sua Test");
        product.setPrice(unitPrice);
        product.setStatus(ProductStatus.ACTIVE);
        product.setStockQuantity(100);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(unitPrice);
        cartItem.setToppings(new ArrayList<>());
        return cartItem;
    }

    // ==========================================
    // TESTS TÍNH PHÍ VẬN CHUYỂN
    // ==========================================

    @Test
    @DisplayName("Case 1: Đơn 52.000đ không voucher = 77.000đ (dưới 150.000đ chịu phí 25.000đ)")
    void testOrderUnder150kWithoutVoucher() {
        CartItem item = createCartItem(new BigDecimal("52000"), 1);
        when(cartItems.findByCartId(testCart.getId())).thenReturn(List.of(item));

        OrderResponse response = orderService.create(1L, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("52000"), savedOrder.getSubtotal());
        assertEquals(BigDecimal.ZERO, savedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("77000"), savedOrder.getTotalAmount());
        assertEquals(new BigDecimal("77000"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Case 2: Đơn 150.000đ không voucher = 150.000đ (đạt 150.000đ được freeship)")
    void testOrderAt150kGetsFreeship() {
        CartItem item = createCartItem(new BigDecimal("75000"), 2);
        when(cartItems.findByCartId(testCart.getId())).thenReturn(List.of(item));

        OrderResponse response = orderService.create(1L, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("150000"), savedOrder.getSubtotal());
        assertEquals(BigDecimal.ZERO, savedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("150000"), savedOrder.getTotalAmount());
        assertEquals(new BigDecimal("150000"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Case 3: Đơn có voucher vẫn tính đúng (subtotal 100k < 150k, voucher 20k, phí ship 25k -> 105.000đ)")
    void testOrderWithVoucherUnder150k() {
        CartItem item = createCartItem(new BigDecimal("100000"), 1);
        when(cartItems.findByCartId(testCart.getId())).thenReturn(List.of(item));

        request.setPromotionCode("DISCOUNT20");
        Promotion promo = new Promotion();
        promo.setCode("DISCOUNT20");
        promo.setName("Giam 20k");
        promo.setStatus(PromotionStatus.ACTIVE);
        promo.setDiscountType(DiscountType.FIXED);
        promo.setDiscountValue(new BigDecimal("20000"));
        promo.setStartAt(LocalDateTime.now().minusDays(1));
        promo.setEndAt(LocalDateTime.now().plusDays(1));

        when(promotions.findByCode("DISCOUNT20")).thenReturn(Optional.of(promo));

        OrderResponse response = orderService.create(1L, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("100000"), savedOrder.getSubtotal());
        assertEquals(new BigDecimal("20000"), savedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("105000"), savedOrder.getTotalAmount());
        assertEquals(new BigDecimal("105000"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Case 4: Đơn có voucher và freeship (subtotal 200k >= 150k, voucher 30k -> 170.000đ)")
    void testOrderWithVoucherOver150k() {
        CartItem item = createCartItem(new BigDecimal("200000"), 1);
        when(cartItems.findByCartId(testCart.getId())).thenReturn(List.of(item));

        request.setPromotionCode("BIGDISCOUNT");
        Promotion promo = new Promotion();
        promo.setCode("BIGDISCOUNT");
        promo.setName("Giam 30k");
        promo.setStatus(PromotionStatus.ACTIVE);
        promo.setDiscountType(DiscountType.FIXED);
        promo.setDiscountValue(new BigDecimal("30000"));
        promo.setStartAt(LocalDateTime.now().minusDays(1));
        promo.setEndAt(LocalDateTime.now().plusDays(1));

        when(promotions.findByCode("BIGDISCOUNT")).thenReturn(Optional.of(promo));

        OrderResponse response = orderService.create(1L, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orders).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(new BigDecimal("200000"), savedOrder.getSubtotal());
        assertEquals(new BigDecimal("30000"), savedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("170000"), savedOrder.getTotalAmount());
        assertEquals(new BigDecimal("170000"), response.getTotalAmount());
    }

    // ==========================================
    // TESTS KHÁCH HÀNG HỦY ĐƠN HÀNG (CANCEL MY ORDER)
    // ==========================================

    @Test
    @DisplayName("Cancel Case 1: Khách hàng hủy đơn PENDING thành công -> trạng thái CANCELLED và hoàn tồn kho")
    void testCancelMyOrder_SuccessWhenPending() {
        Long orderId = 100L;
        Order order = new Order();
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, orderId);
        } catch (Exception ignored) {}
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(new BigDecimal("60000"));
        order.setTotalAmount(new BigDecimal("85000"));

        Product product = new Product();
        product.setName("Tra Sua Oolong");
        product.setStockQuantity(10); // ban đầu còn 10 món

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(3); // đơn hàng mua 3 món

        when(orders.findByIdWithLock(orderId)).thenReturn(Optional.of(order));
        when(orderItems.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse res = orderService.cancelMyOrder(1L, orderId);

        // Kiểm tra trạng thái đơn hàng đã đổi thành CANCELLED
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(OrderStatus.CANCELLED, res.getStatus());

        // Kiểm tra tồn kho đã được hoàn trả chính xác: 10 + 3 = 13
        assertEquals(13, product.getStockQuantity());
        verify(products, times(1)).save(product);
        verify(orders, times(1)).save(order);
    }

    @Test
    @DisplayName("Cancel Case 2: Từ chối hủy đơn khi đơn hàng thuộc về người dùng khác (Bảo mật / Phân quyền)")
    void testCancelMyOrder_FailsWhenNotOwner() {
        Long orderId = 101L;

        User anotherUser = new User();
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(anotherUser, 2L); // user khác id = 2
        } catch (Exception ignored) {}

        Order order = new Order();
        order.setUser(anotherUser);
        order.setStatus(OrderStatus.PENDING);

        when(orders.findByIdWithLock(orderId)).thenReturn(Optional.of(order));

        // Khách hàng id = 1 cố tình hủy đơn của khách id = 2
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                orderService.cancelMyOrder(1L, orderId)
        );

        assertEquals("Bạn không có quyền hủy đơn hàng này", ex.getMessage());
        // Đảm bảo không thay đổi trạng thái và không hoàn tồn kho
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(products, never()).save(any());
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("Cancel Case 3: Từ chối hủy đơn khi đơn không còn ở trạng thái PENDING (CONFIRMED, SHIPPING, COMPLETED, CANCELLED)")
    void testCancelMyOrder_FailsWhenNotPending() {
        Long orderId = 102L;

        OrderStatus[] nonPendingStatuses = {
                OrderStatus.CONFIRMED,
                OrderStatus.SHIPPING,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
        };

        for (OrderStatus st : nonPendingStatuses) {
            Order order = new Order();
            order.setUser(testUser);
            order.setStatus(st);

            when(orders.findByIdWithLock(orderId)).thenReturn(Optional.of(order));

            BadRequestException ex = assertThrows(BadRequestException.class, () ->
                    orderService.cancelMyOrder(1L, orderId)
            );

            assertEquals("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận (PENDING)", ex.getMessage());
            verify(products, never()).save(any());
        }
    }

    @Test
    @DisplayName("Cancel Case 4: Ném ResourceNotFoundException khi không tìm thấy đơn hàng")
    void testCancelMyOrder_FailsWhenNotFound() {
        Long orderId = 999L;
        when(orders.findByIdWithLock(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.cancelMyOrder(1L, orderId)
        );

        verify(products, never()).save(any());
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("Concurrency Case 1: Hai yêu cầu hủy cùng một đơn PENDING -> Request 1 thành công hoàn kho 1 lần, Request 2 bị chặn không hoàn kho lần 2")
    void testConcurrentCancellation_PreventDoubleRefund() {
        Long orderId = 103L;
        Order order = new Order();
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, orderId);
        } catch (Exception ignored) {}
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);

        Product product = new Product();
        product.setName("Tra Xanh");
        product.setStockQuantity(20);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(5);

        when(orders.findByIdWithLock(orderId)).thenReturn(Optional.of(order));
        when(orderItems.findByOrderId(orderId)).thenReturn(List.of(orderItem));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Request 1: Hủy thành công
        OrderResponse res1 = orderService.cancelMyOrder(1L, orderId);
        assertEquals(OrderStatus.CANCELLED, res1.getStatus());
        assertEquals(25, product.getStockQuantity()); // Hoàn 5: 20 + 5 = 25
        verify(products, times(1)).save(product);

        // Request 2 (đến sau khi Request 1 giải phóng lock, thấy status là CANCELLED):
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                orderService.cancelMyOrder(1L, orderId)
        );
        assertEquals("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận (PENDING)", ex.getMessage());

        // Tồn kho vẫn là 25, tuyệt đối không bị cộng lần 2
        assertEquals(25, product.getStockQuantity());
        verify(products, times(1)).save(product); // Vẫn chỉ được save đúng 1 lần
    }

    @Test
    @DisplayName("Concurrency Case 2: Customer hủy trong khi Admin đang cập nhật cùng đơn -> Tuân thủ đúng thứ tự qua lock")
    void testCustomerCancelVsAdminUpdateConflict() {
        Long orderId = 104L;
        Order order = new Order();
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, orderId);
        } catch (Exception ignored) {}
        order.setUser(testUser);
        order.setStatus(OrderStatus.PENDING);

        when(orders.findByIdWithLock(orderId)).thenReturn(Optional.of(order));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Giả sử Admin được giữ lock trước và xác nhận đơn hàng sang CONFIRMED:
        orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        // Sau đó Customer có được lock và đọc trạng thái mới nhất:
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                orderService.cancelMyOrder(1L, orderId)
        );
        assertEquals("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận (PENDING)", ex.getMessage());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }
}
