package Web_Drink_Store.webstore.entity;

import Web_Drink_Store.webstore.enums.DrinkIce;
import Web_Drink_Store.webstore.enums.DrinkSize;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * CartItem lưu cấu hình đầy đủ của từng ly đồ uống.
 * Unique constraint cũ (cart_id + product_id) đã được xóa vì
 * cùng product nhưng khác option sẽ là CartItem khác nhau.
 * Merge chỉ xảy ra khi product + size + sweetness + ice + toppings giống hoàn toàn.
 *
 * DB: Hibernate ddl-auto=update sẽ:
 *   - Xóa unique constraint cũ (cart_id, product_id)
 *   - Thêm các cột mới: size, sweetness, ice, unit_price
 *   - Tạo bảng cart_item_toppings
 *
 * MIGRATION NOTE: Dữ liệu cart_items cũ sẽ có size=NULL, sweetness=NULL, ice=NULL,
 * unit_price=NULL, toppings=rỗng. Chúng vẫn tồn tại nhưng sẽ không hiển thị đúng
 * option. Nên clear cart cũ sau khi restart backend, hoặc xử lý thủ công nếu cần.
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private DrinkSize size = DrinkSize.M;

    /**
     * Sweetness (0, 30, 50, 70, 100).
     * 0 = Không ngọt — là giá trị HỢP LỆ, không phải null.
     */
    @Column(nullable = false)
    private Integer sweetness = 70;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DrinkIce ice = DrinkIce.NORMAL_ICE;

    /**
     * Toppings được lưu dưới dạng embedded collection.
     * Bảng cart_item_toppings với FK → cart_items.id.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cart_item_toppings",
            joinColumns = @JoinColumn(name = "cart_item_id")
    )
    private List<ToppingItem> toppings = new ArrayList<>();

    /**
     * Unit price đã bao gồm: product.price + size surcharge + tổng topping surcharge.
     * Được backend tính, không nhận từ frontend.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public DrinkSize getSize() { return size; }
    public void setSize(DrinkSize size) { this.size = size; }

    public Integer getSweetness() { return sweetness; }
    public void setSweetness(Integer sweetness) { this.sweetness = sweetness; }

    public DrinkIce getIce() { return ice; }
    public void setIce(DrinkIce ice) { this.ice = ice; }

    public List<ToppingItem> getToppings() { return toppings; }
    public void setToppings(List<ToppingItem> toppings) { this.toppings = toppings; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
