package Web_Drink_Store.webstore.repository;

import Web_Drink_Store.webstore.dto.statistics.ProductStatsResponse;
import Web_Drink_Store.webstore.entity.OrderItem;
import Web_Drink_Store.webstore.enums.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT DISTINCT oi
        FROM OrderItem oi
        LEFT JOIN FETCH oi.toppings
        WHERE oi.order.id = :orderId
    """)
    List<OrderItem> findByOrderId(
            @Param("orderId") Long orderId
    );

    // =========================
    // SẢN PHẨM BÁN CHẠY
    // Chỉ tính đơn COMPLETED
    // =========================
    @Query("""
        SELECT new Web_Drink_Store.webstore.dto.statistics.ProductStatsResponse(
            oi.product.id,
            oi.product.name,
            SUM(oi.quantity),
            SUM(oi.lineTotal)
        )
        FROM OrderItem oi
        WHERE oi.order.status = :status
        GROUP BY oi.product.id, oi.product.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<ProductStatsResponse> findBestSellingProducts(
            @Param("status") OrderStatus status
    );
}