package Web_Drink_Store.webstore.dto.cart;

import java.math.BigDecimal;

/**
 * Topping được gửi từ frontend khi thêm vào giỏ hàng.
 * Mapping theo TOPPINGS trong mockData.js:
 *   { id: 'tapioca', name: 'Trân châu đen', price: 5000 }
 */
public class ToppingRequest {
    private String id;
    private String name;
    private BigDecimal price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
