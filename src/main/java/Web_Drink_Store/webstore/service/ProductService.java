package Web_Drink_Store.webstore.service;

import Web_Drink_Store.webstore.dto.product.*;
import java.util.List;

public interface ProductService {

    List<ProductResponse> getAll();

    List<ProductResponse> getActive(Long categoryId);

    List<ProductResponse> search(String keyword, Long categoryId);

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void deactivate(Long id);
}