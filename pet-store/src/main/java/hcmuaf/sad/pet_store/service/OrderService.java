package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // Customer
    Page<OrderResponse> getMyOrders(Long userId, Pageable pageable);
    OrderResponse getMyOrderDetail(Long orderId, Long userId);

    // Admin
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse getOrderDetailForAdmin(Long orderId);
}
