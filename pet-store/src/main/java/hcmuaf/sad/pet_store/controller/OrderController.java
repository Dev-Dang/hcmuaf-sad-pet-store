package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.OrderResponse;
import hcmuaf.sad.pet_store.entity.User;
import hcmuaf.sad.pet_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // --- Customer Endpoints ---
    @GetMapping("/orders/me")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(orderService.getMyOrders(user.getId(), pageable));
    }

    @GetMapping("/orders/me/{id}")
    public ApiResponse<OrderResponse> getMyOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(orderService.getMyOrderDetail(id, user.getId()));
    }

    // --- Admin Endpoints ---
    @GetMapping("/admin/orders")
    public ApiResponse<Page<OrderResponse>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(orderService.getAllOrders(pageable));
    }

    @GetMapping("/admin/orders/{id}")
    public ApiResponse<OrderResponse> getOrderDetailForAdmin(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderDetailForAdmin(id));
    }
}
