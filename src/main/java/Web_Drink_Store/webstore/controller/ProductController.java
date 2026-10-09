package Web_Drink_Store.webstore.controller;

import Web_Drink_Store.webstore.dto.ApiResponse;
import Web_Drink_Store.webstore.dto.product.ProductRequest;
import Web_Drink_Store.webstore.dto.product.ProductResponse;
import Web_Drink_Store.webstore.exception.UnauthorizedException;
import Web_Drink_Store.webstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    private void admin(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            throw new UnauthorizedException("Cần quyền ADMIN");
        }
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> all(
            @RequestParam(required = false) Long categoryId,
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
                service.getActive(categoryId)
        );
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.ok(
                "OK",
                service.search(keyword, categoryId)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> one(
            @PathVariable Long id
    ) {
        return ApiResponse.ok(
                "OK",
                service.getById(id)
        );
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(
            @RequestBody ProductRequest request,
            HttpSession session
    ) {
        admin(session);

        return ApiResponse.ok(
                "Tạo thành công",
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody ProductRequest request,
            HttpSession session
    ) {
        admin(session);

        return ApiResponse.ok(
                "Cập nhật thành công",
                service.update(id, request)
        );
    }

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