package Web_Drink_Store.webstore.dto.cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response trả về thông tin đầy đủ CartItem bao gồm option và giá tính đúng.
 *
 * price = unitPrice đã bao gồm size + toppings surcharge.
 * lineTotal = price × quantity.
 * size: "M" | "L" | "XL"
 * sweetness: 0 | 30 | 50 | 70 | 100
 * ice: "HOT" | "LESS_ICE" | "NORMAL_ICE" | "FULL_ICE"
 * toppings: list ToppingResponse
 */
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;

    /** Unit price đã bao gồm size + toppings surcharge */
    private BigDecimal price;

    private Integer quantity;
    private BigDecimal lineTotal;

    private String size;
    private Integer sweetness;
    private String ice;
    private List<ToppingResponse> toppings;

    public CartItemResponse(
            Long id,
            Long productId,
            String productName,
            BigDecimal price,
            Integer quantity,
            BigDecimal lineTotal,
            String size,
            Integer sweetness,
            String ice,
            List<ToppingResponse> toppings
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.size = size;
        this.sweetness = sweetness;
        this.ice = ice;
        this.toppings = toppings;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getSize() { return size; }
    public Integer getSweetness() { return sweetness; }
    public String getIce() { return ice; }
    public List<ToppingResponse> getToppings() { return toppings; }
}
