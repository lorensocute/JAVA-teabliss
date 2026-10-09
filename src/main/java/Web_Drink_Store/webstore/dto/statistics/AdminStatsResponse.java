package Web_Drink_Store.webstore.dto.statistics;

import java.math.BigDecimal;

public class AdminStatsResponse {

    private long totalUsers;
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;

    public AdminStatsResponse(
            long totalUsers,
            long totalProducts,
            long activeProducts,
            long totalOrders,
            BigDecimal totalRevenue
    ) {
        this.totalUsers = totalUsers;
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public AdminStatsResponse(
            long totalUsers,
            long totalProducts,
            long totalOrders,
            BigDecimal totalRevenue
    ) {
        this(totalUsers, totalProducts, totalProducts, totalOrders, totalRevenue);
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
