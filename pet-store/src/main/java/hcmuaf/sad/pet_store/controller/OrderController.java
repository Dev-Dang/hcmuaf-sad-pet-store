package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import hcmuaf.sad.pet_store.dto.response.OrderResponse;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // --- Customer Endpoints ---
    @GetMapping("/orders/me")
    public ApiResponse<Page<OrderResponse>> getMyOrders(HttpSession session,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(orderService.getMyOrders(user.getUserCode(), pageable));
    }

    @GetMapping("/orders/me/{id}")
    public ApiResponse<OrderResponse> getMyOrderDetail(HttpSession session,
                                                       @PathVariable Long id) {
        User user = resolveCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("UNAUTHORIZED", "Vui long dang nhap.");
        }
        return ApiResponse.success(orderService.getMyOrderDetail(id, user.getUserCode()));
    }

    // --- Admin Endpoints ---
    @GetMapping("/admin/orders")
    public ApiResponse<Page<OrderResponse>> getAllOrdersForAdmin(HttpSession session,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        if (!isAdmin(session)) {
            return ApiResponse.error("FORBIDDEN", "Ban khong co quyen truy cap.");
        }
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(orderService.getAllOrders(pageable));
    }

    @GetMapping("/admin/orders/{id}")
    public ApiResponse<OrderResponse> getOrderDetailForAdmin(HttpSession session,
                                                             @PathVariable Long id) {
        if (!isAdmin(session)) {
            return ApiResponse.error("FORBIDDEN", "Ban khong co quyen truy cap.");
        }
        return ApiResponse.success(orderService.getOrderDetailForAdmin(id));
    }

    private User resolveCurrentUser(HttpSession session) {
        String userCode = (String) session.getAttribute("userCode");
        if (userCode == null || userCode.isBlank()) {
            return null;
        }
        return User.findActiveByUserCode(userCode);
    }

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
