package Web_Drink_Store.webstore.controller;

import Web_Drink_Store.webstore.dto.ApiResponse;
import Web_Drink_Store.webstore.dto.category.CategoryRequest;
import Web_Drink_Store.webstore.dto.category.CategoryResponse;
import Web_Drink_Store.webstore.exception.UnauthorizedException;
import Web_Drink_Store.webstore.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    private void admin(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            throw new UnauthorizedException("Cần quyền ADMIN");
        }
    }

    // Lấy danh sách category (ACTIVE cho khách, all cho ADMIN)
    @GetMapping
    public ApiResponse<List<CategoryResponse>> all(
            @RequestParam(required = false) Boolean all,
            HttpSession session
    ) {
        if (Boolean.TRUE.equals(all) && "ADMIN".equals(session.getAttribute("role"))) {
            return ApiResponse.ok(
                    "OK",
                    service.getAll()
            );
        }

        return ApiResponse.ok(
                "OK",
                service.getActive()
        );
    }

    // Lấy chi tiết category theo ID
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> one(
            @PathVariable Long id
    ) {
        return ApiResponse.ok(
                "OK",
                service.getById(id)
        );
    }

    // Tạo category
    @PostMapping
    public ApiResponse<CategoryResponse> create(
            @RequestBody CategoryRequest request,
            HttpSession session
    ) {
        admin(session);

        return ApiResponse.ok(
                "Tạo thành công",
                service.create(request)
        );
    }

    // Cập nhật category
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable Long id,
            @RequestBody CategoryRequest request,
            HttpSession session
    ) {
        admin(session);

        return ApiResponse.ok(
                "Cập nhật thành công",
                service.update(id, request)
        );
    }

    // Soft delete category
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            HttpSession session
    ) {
        admin(session);

        service.deactivate(id);

        return ApiResponse.ok(
                "Đã chuyển sang INACTIVE",
                null
        );
    }
}