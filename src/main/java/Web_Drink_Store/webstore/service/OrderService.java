package Web_Drink_Store.webstore.service;

import Web_Drink_Store.webstore.dto.order.OrderRequest;
import Web_Drink_Store.webstore.dto.order.OrderResponse;
import Web_Drink_Store.webstore.enums.OrderStatus;

import java.util.List;
public interface OrderService {
    OrderResponse create(
            Long userId,
            OrderRequest request
    );
// user xem các đơn của mình
    List<OrderResponse> getMyOrders(Long userId);
//user xem chi tiết 1 đơn của mình
    OrderResponse getMyOrderById(
        Long userId,
        Long orderId
);
    List<OrderResponse> getAll();

    OrderResponse updateStatus(
            Long orderId,
            OrderStatus status
    );

    OrderResponse cancelMyOrder(
            Long userId,
            Long orderId
    );
}