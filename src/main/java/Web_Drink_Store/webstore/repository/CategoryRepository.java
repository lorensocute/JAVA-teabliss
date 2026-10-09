package Web_Drink_Store.webstore.repository;

import Web_Drink_Store.webstore.entity.Category;
import Web_Drink_Store.webstore.enums.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStatus(CategoryStatus status);

    boolean existsByName(String name);
    
    java.util.Optional<Category> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}