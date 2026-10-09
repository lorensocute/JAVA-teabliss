package Web_Drink_Store.webstore.dto.order;

import Web_Drink_Store.webstore.dto.cart.ToppingResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Snapshot đầy đủ một item trong đơn hàng.
 * Các field option (size/sweetness/ice/toppings) là nullable — đơn cũ không có data.
 */
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    /** Snapshot options — nullable nếu đơn cũ không lưu */
    private String size;
    private Integer sweetness;
    private String ice;
    private List<ToppingResponse> toppings;

    public OrderItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String size,
            Integer sweetness,
            String ice,
            List<ToppingResponse> toppings
    ) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.size = size;
        this.sweetness = sweetness;
        this.ice = ice;
        this.toppings = toppings;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getSize() { return size; }
    public Integer getSweetness() { return sweetness; }
    public String getIce() { return ice; }
    public List<ToppingResponse> getToppings() { return toppings; }
}
