package Web_Drink_Store.webstore.repository;

import Web_Drink_Store.webstore.entity.Product;
import Web_Drink_Store.webstore.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(ProductStatus status);

    long countByStatus(ProductStatus status);

    boolean existsByName(String name);

    List<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatus status);

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = :status
        AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    List<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status
    );
}