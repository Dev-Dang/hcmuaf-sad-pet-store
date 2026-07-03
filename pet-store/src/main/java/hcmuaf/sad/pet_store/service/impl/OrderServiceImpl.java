package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.dto.response.OrderResponse;
import hcmuaf.sad.pet_store.entity.Order;
import hcmuaf.sad.pet_store.entity.OrderItem;
import hcmuaf.sad.pet_store.exception.resource.ResourceNotFoundException;
import hcmuaf.sad.pet_store.repository.OrderRepository;
import hcmuaf.sad.pet_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                .map(o -> toResponse(o, false));
    }

    @Override
    public OrderResponse getMyOrderDetail(Long orderId, Long userId) {
        // BRULE-08: Customer can only view their own orders (EX.2)
        Order order = orderRepository.findByIdAndUserIdAndDeletedAtIsNull(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Đơn hàng không tồn tại hoặc bạn không có quyền truy cập"));
        return toResponse(order, false);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllActiveOrders(pageable)
                .map(o -> toResponse(o, true));
    }

    @Override
    public OrderResponse getOrderDetailForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order, true);
    }

    private OrderResponse toResponse(Order order, boolean includeCustomerInfo) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .filter(item -> item.getDeletedAt() == null)
                        .map(this::toItemResponse)
                        .collect(Collectors.toList());

        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .orderStatus(order.getOrderStatus().name())
                .orderStatusLabel(order.getOrderStatus().getLabel())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentStatusLabel(order.getPaymentStatus().getLabel())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .items(itemResponses);

        if (includeCustomerInfo && order.getUser() != null) {
            builder.customerEmail(order.getUser().getEmail())
                   .customerName(order.getUser().getFullName());
        }

        return builder.build();
    }

    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}
