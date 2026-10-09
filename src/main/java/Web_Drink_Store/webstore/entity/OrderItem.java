package Web_Drink_Store.webstore.entity;

import Web_Drink_Store.webstore.enums.DrinkIce;
import Web_Drink_Store.webstore.enums.DrinkSize;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderItem lưu snapshot đầy đủ cấu hình ly đồ uống tại thời điểm checkout.
 * Sau khi đặt hàng, dù sản phẩm/giá thay đổi, OrderItem vẫn giữ đúng thông tin cũ.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotal;

    // ── Option snapshot ─────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private DrinkSize size;

    private Integer sweetness;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private DrinkIce ice;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "order_item_toppings",
            joinColumns = @JoinColumn(name = "order_item_id")
    )
    private List<ToppingItem> toppings = new ArrayList<>();

    // ── Getters / Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    public DrinkSize getSize() { return size; }
    public void setSize(DrinkSize size) { this.size = size; }

    public Integer getSweetness() { return sweetness; }
    public void setSweetness(Integer sweetness) { this.sweetness = sweetness; }

    public DrinkIce getIce() { return ice; }
    public void setIce(DrinkIce ice) { this.ice = ice; }

    public List<ToppingItem> getToppings() { return toppings; }
    public void setToppings(List<ToppingItem> toppings) { this.toppings = toppings; }
}
