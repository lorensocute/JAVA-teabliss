package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.product.ProductRequest;
import Web_Drink_Store.webstore.dto.product.ProductResponse;
import Web_Drink_Store.webstore.entity.Category;
import Web_Drink_Store.webstore.entity.Product;
import Web_Drink_Store.webstore.enums.CategoryStatus;
import Web_Drink_Store.webstore.enums.ProductStatus;
import Web_Drink_Store.webstore.exception.BadRequestException;
import Web_Drink_Store.webstore.exception.ResourceNotFoundException;
import Web_Drink_Store.webstore.repository.CategoryRepository;
import Web_Drink_Store.webstore.repository.ProductRepository;
import Web_Drink_Store.webstore.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final CategoryRepository categories;

    public ProductServiceImpl(
            ProductRepository repo,
            CategoryRepository categories
    ) {
        this.repo = repo;
        this.categories = categories;
    }

    private Product get(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy product"
                        )
                );
    }

    private ProductResponse map(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getImageUrl(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getStatus()
        );
    }

    @Override
    public List<ProductResponse> getAll() {
        return repo.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<ProductResponse> getActive(Long categoryId) {

        List<Product> list = categoryId == null
                ? repo.findByStatus(ProductStatus.ACTIVE)
                : repo.findByCategoryIdAndStatus(
                categoryId,
                ProductStatus.ACTIVE
        );

        return list.stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<ProductResponse> search(
            String keyword,
            Long categoryId
    ) {

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        return repo.searchAndFilter(
                        keyword,
                        categoryId,
                        ProductStatus.ACTIVE
                )
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public ProductResponse getById(Long id) {
        return map(get(id));
    }

    @Override
    public ProductResponse create(ProductRequest request) {

        Product product = new Product();

        fill(product, request);

        product.setStatus(ProductStatus.ACTIVE);

        return map(repo.save(product));
    }

    @Override
    public ProductResponse update(
            Long id,
            ProductRequest request
    ) {

        Product product = get(id);

        fill(product, request);

        return map(repo.save(product));
    }

    @Override
    public void deactivate(Long id) {

        Product product = get(id);

        if (product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.INACTIVE);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }

        repo.save(product);
    }

    private void fill(
            Product product,
            ProductRequest request
    ) {

        // Kiểm tra giá và số lượng tồn kho
        if (request.getPrice() == null
                || request.getPrice().signum() < 0
                || request.getStockQuantity() == null
                || request.getStockQuantity() < 0) {

            throw new BadRequestException(
                    "Giá hoặc tồn kho không hợp lệ"
            );
        }

        // Kiểm tra category có tồn tại không
        Category category = categories
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy category"
                        )
                );

        // Không cho Product thuộc Category đã INACTIVE
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new BadRequestException(
                    "Category không hoạt động"
            );
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setStockQuantity(
                request.getStockQuantity()
        );
        product.setCategory(category);
    }
}