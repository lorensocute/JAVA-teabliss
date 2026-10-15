package Web_Drink_Store.webstore.repository;

import Web_Drink_Store.webstore.entity.Order;
import Web_Drink_Store.webstore.enums.OrderStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy Order và khóa dòng (SELECT ... FOR UPDATE) để tránh race condition khi cập nhật trạng thái hoặc hủy đơn
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    // User xem danh sách đơn hàng của mình
    List<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    // User xem chi tiết một đơn hàng của mình
    Optional<Order> findByIdAndUserId(
            Long id,
            Long userId
    );

    // =========================
    // ĐẾM ORDER THEO STATUS
    // =========================
    long countByStatus(
            OrderStatus status
    );

    // =========================
    // TỔNG DOANH THU
    // Chỉ tính đơn COMPLETED
    // =========================
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = :status
    """)
    BigDecimal sumTotalAmountByStatus(
            @Param("status")
            OrderStatus status
    );

    // =========================
    // DOANH THU THEO KHOẢNG THỜI GIAN
    // =========================
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = :status
        AND o.createdAt >= :from
        AND o.createdAt < :to
    """)
    BigDecimal sumRevenueByStatusAndCreatedAt(
            @Param("status")
            OrderStatus status,

            @Param("from")
            LocalDateTime from,

            @Param("to")
            LocalDateTime to
    );
}