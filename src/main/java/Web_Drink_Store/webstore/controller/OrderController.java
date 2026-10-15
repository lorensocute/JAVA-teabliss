package Web_Drink_Store.webstore.controller;

import Web_Drink_Store.webstore.dto.ApiResponse;
import Web_Drink_Store.webstore.dto.order.OrderRequest;
import Web_Drink_Store.webstore.dto.order.OrderResponse;
import Web_Drink_Store.webstore.exception.UnauthorizedException;
import Web_Drink_Store.webstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService s){
        service=s;}
    private Long uid(HttpSession s){
        Object v=s.getAttribute("userId");
        if(v==null)throw new UnauthorizedException("Chưa đăng nhập");
        return (Long) v;}
    @PostMapping
    public ApiResponse<OrderResponse> create(
            @RequestBody OrderRequest r,
            HttpSession s){
        return ApiResponse.ok(
                "Đặt hàng thành công",
                service.create(uid(s),r));}
    @GetMapping
    public ApiResponse<List<OrderResponse>> mine(
            HttpSession s){
        return ApiResponse.ok(
                "OK",
                service.getMyOrders(uid(s)));}
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(
            @PathVariable Long id,
            HttpSession s
    ) {
        return ApiResponse.ok(
                "OK",
                service.getMyOrderById(uid(s), id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(
            @PathVariable Long id,
            HttpSession s
    ) {
        return ApiResponse.ok(
                "Hủy đơn hàng thành công",
                service.cancelMyOrder(uid(s), id));
    }
}

