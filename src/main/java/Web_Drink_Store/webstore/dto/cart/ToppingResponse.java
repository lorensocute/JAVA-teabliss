package Web_Drink_Store.webstore.dto.cart;

import java.math.BigDecimal;

/**
 * Topping được trả về trong CartItemResponse và OrderItemResponse.
 */
public class ToppingResponse {
    private String id;
    private String name;
    private BigDecimal price;

    public ToppingResponse(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
}
