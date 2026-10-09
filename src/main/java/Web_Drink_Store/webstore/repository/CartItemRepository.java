package Web_Drink_Store.webstore.repository;

import Web_Drink_Store.webstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    /**
     * Xóa hết toàn bộ item của một cart (dùng khi clear cart hoặc checkout).
     */
    void deleteByCartId(Long cartId);
}
