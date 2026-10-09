package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.category.CategoryRequest;
import Web_Drink_Store.webstore.dto.category.CategoryResponse;
import Web_Drink_Store.webstore.entity.Category;
import Web_Drink_Store.webstore.enums.CategoryStatus;
import Web_Drink_Store.webstore.exception.BadRequestException;
import Web_Drink_Store.webstore.exception.ResourceNotFoundException;
import Web_Drink_Store.webstore.repository.CategoryRepository;
import Web_Drink_Store.webstore.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    private Category get(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy category"
                        )
                );
    }

    private CategoryResponse map(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getStatus()
        );
    }

    @Override
    public List<CategoryResponse> getAll() {
        return repo.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<CategoryResponse> getActive() {
        return repo.findByStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(this::map)
                .toList();
    }

    // Lấy category theo ID
    @Override
    public CategoryResponse getById(Long id) {
        return map(get(id));
    }

    @Override
    public CategoryResponse create(CategoryRequest r) {

        if (repo.existsByName(r.getName())) {
            throw new BadRequestException(
                    "Tên category đã tồn tại"
            );
        }

        Category c = new Category();

        c.setName(r.getName());
        c.setDescription(r.getDescription());
        c.setStatus(CategoryStatus.ACTIVE);

        return map(repo.save(c));
    }

    @Override
    public CategoryResponse update(
            Long id,
            CategoryRequest r
    ) {

        Category c = get(id);

        // Không cho trùng tên với category khác
        if (repo.existsByNameAndIdNot(r.getName(), id)) {
            throw new BadRequestException(
                    "Tên category đã tồn tại"
            );
        }

        c.setName(r.getName());
        c.setDescription(r.getDescription());

        return map(repo.save(c));
    }

    @Override
    public void deactivate(Long id) {

        Category c = get(id);

        if (c.getStatus() == CategoryStatus.ACTIVE) {
            c.setStatus(CategoryStatus.INACTIVE);
        } else {
            c.setStatus(CategoryStatus.ACTIVE);
        }

        repo.save(c);
    }
}