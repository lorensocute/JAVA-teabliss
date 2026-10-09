package Web_Drink_Store.webstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Embedded topping snapshot.
 * Lưu trong cart_item_toppings / order_item_toppings via @ElementCollection.
 *
 * Mapping theo TOPPINGS trong mockData.js:
 *   tapioca        → Trân châu đen     → 5000
 *   white-tapioca  → Trân châu trắng   → 5000
 *   pudding        → Pudding trứng      → 8000
 *   jelly          → Thạch dừa          → 5000
 *   cheese-foam    → Foam phô mai       → 10000
 *   aloe           → Nha đam            → 7000
 *   red-bean       → Đậu đỏ             → 6000
 *   taro-ball      → Khoai môn viên     → 8000
 */
@Embeddable
public class ToppingItem {

    @Column(nullable = false)
    private String toppingId;

    @Column(nullable = false)
    private String toppingName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    public ToppingItem() {}

    public ToppingItem(String toppingId, String toppingName, BigDecimal price) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.price = price;
    }

    public String getToppingId() { return toppingId; }
    public void setToppingId(String toppingId) { this.toppingId = toppingId; }
    public String getToppingName() { return toppingName; }
    public void setToppingName(String toppingName) { this.toppingName = toppingName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
