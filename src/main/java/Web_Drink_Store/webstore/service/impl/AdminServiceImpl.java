package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.statistics.AdminStatsResponse;
import Web_Drink_Store.webstore.dto.statistics.OrderStatsResponse;
import Web_Drink_Store.webstore.dto.statistics.ProductStatsResponse;

import Web_Drink_Store.webstore.enums.OrderStatus;
import Web_Drink_Store.webstore.enums.ProductStatus;

import Web_Drink_Store.webstore.exception.BadRequestException;

import Web_Drink_Store.webstore.repository.OrderItemRepository;
import Web_Drink_Store.webstore.repository.OrderRepository;
import Web_Drink_Store.webstore.repository.ProductRepository;
import Web_Drink_Store.webstore.repository.UserRepository;

import Web_Drink_Store.webstore.service.AdminService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // =========================
    // THỐNG KÊ TỔNG QUAN
    // =========================
    @Override
    public AdminStatsResponse getStatistics() {

        long totalUsers =
                userRepository.count();

        long totalProducts =
                productRepository.count();

        long activeProducts =
                productRepository.countByStatus(
                        ProductStatus.ACTIVE
                );

        long totalOrders =
                orderRepository.count();

        BigDecimal totalRevenue =
                orderRepository.sumTotalAmountByStatus(
                        OrderStatus.COMPLETED
                );

        return new AdminStatsResponse(
                totalUsers,
                totalProducts,
                activeProducts,
                totalOrders,
                totalRevenue
        );
    }

    // =========================
    // DOANH THU THEO KHOẢNG NGÀY
    // =========================
    @Override
    public BigDecimal getRevenue(
            LocalDate from,
            LocalDate to
    ) {

        if (from == null || to == null) {

            throw new BadRequestException(
                    "from và to không được để trống"
            );
        }

        if (from.isAfter(to)) {

            throw new BadRequestException(
                    "from không được lớn hơn to"
            );
        }

        LocalDateTime fromDateTime =
                from.atStartOfDay();

        LocalDateTime toDateTime =
                to.plusDays(1)
                        .atStartOfDay();

        return orderRepository
                .sumRevenueByStatusAndCreatedAt(
                        OrderStatus.COMPLETED,
                        fromDateTime,
                        toDateTime
                );
    }

    // =========================
    // THỐNG KÊ ORDER
    // =========================
    @Override
    public OrderStatsResponse getOrderStatistics() {

        long totalOrders =
                orderRepository.count();

        long pending =
                orderRepository.countByStatus(
                        OrderStatus.PENDING
                );

        long confirmed =
                orderRepository.countByStatus(
                        OrderStatus.CONFIRMED
                );

        long shipping =
                orderRepository.countByStatus(
                        OrderStatus.SHIPPING
                );

        long completed =
                orderRepository.countByStatus(
                        OrderStatus.COMPLETED
                );

        long cancelled =
                orderRepository.countByStatus(
                        OrderStatus.CANCELLED
                );

        return new OrderStatsResponse(
                totalOrders,
                pending,
                confirmed,
                shipping,
                completed,
                cancelled
        );
    }

    // =========================
    // SẢN PHẨM BÁN CHẠY
    // =========================
    @Override
    public List<ProductStatsResponse>
    getProductStatistics() {

        return orderItemRepository
                .findBestSellingProducts(
                        OrderStatus.COMPLETED
                );
    }
}