package Web_Drink_Store.webstore.dto.statistics;

import java.math.BigDecimal;

public class ProductStatsResponse {

    private Long productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;

    public ProductStatsResponse(
            Long productId,
            String productName,
            Long totalQuantitySold,
            BigDecimal totalRevenue
    ) {
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public ProductStatsResponse(
            Long productId,
            String productName,
            Long totalQuantitySold
    ) {
        this(productId, productName, totalQuantitySold, BigDecimal.ZERO);
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}