package Web_Drink_Store.webstore.dto.cart;

import java.util.List;

/**
 * Request gửi từ frontend khi thêm item vào giỏ hàng.
 * Frontend KHÔNG gửi price — backend tự tính unitPrice.
 *
 * sweetness = 0 là hợp lệ (Không ngọt). Không validate bằng !sweetness.
 *
 * Ví dụ JSON:
 * {
 *   "productId": 1,
 *   "quantity": 1,
 *   "size": "L",
 *   "sweetness": 0,
 *   "ice": "NORMAL_ICE",
 *   "toppings": [{"id":"tapioca","name":"Trân châu đen","price":5000}]
 * }
 */
public class CartItemRequest {

    private Long productId;
    private Integer quantity;

    /** Size: M | L | XL */
    private String size;

    /** Sweetness: 0, 30, 50, 70, 100. 0 = Không ngọt (hợp lệ). */
    private Integer sweetness;

    /** Ice: HOT | LESS_ICE | NORMAL_ICE | FULL_ICE */
    private String ice;

    /** Toppings frontend gửi lên (id + name + price). */
    private List<ToppingRequest> toppings;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public Integer getSweetness() { return sweetness; }
    public void setSweetness(Integer sweetness) { this.sweetness = sweetness; }

    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }

    public List<ToppingRequest> getToppings() { return toppings; }
    public void setToppings(List<ToppingRequest> toppings) { this.toppings = toppings; }
}
