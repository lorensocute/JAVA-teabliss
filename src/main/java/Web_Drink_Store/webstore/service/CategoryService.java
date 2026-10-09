package Web_Drink_Store.webstore.service;

import Web_Drink_Store.webstore.dto.category.CategoryRequest;
import Web_Drink_Store.webstore.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAll();

    List<CategoryResponse> getActive();

    CategoryResponse getById(Long id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void deactivate(Long id);
}