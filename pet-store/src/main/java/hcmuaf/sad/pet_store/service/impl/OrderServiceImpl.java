package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.dto.response.OrderResponse;
import hcmuaf.sad.pet_store.entity.Order;
import hcmuaf.sad.pet_store.entity.OrderItem;
import hcmuaf.sad.pet_store.exception.ResourceNotFoundException;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.repository.OrderRepository;
import hcmuaf.sad.pet_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Page<OrderResponse> getMyOrders(String userCode, Pageable pageable) {
        return orderRepository.findByUserCodeAndDeletedAtIsNullOrderByCreatedAtDesc(userCode, pageable)
                .map(o -> toResponse(o, false));
    }

    @Override
    public OrderResponse getMyOrderDetail(Long orderId, String userCode) {
        // BRULE-08: Customer can only view their own orders (EX.2)
        Order order = orderRepository.findByIdAndUserCodeAndDeletedAtIsNull(orderId, userCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
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
                .orderCode(order.getOrderCode())
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

        if (includeCustomerInfo) {
            User customer = User.findById(order.getUserId());
            if (customer != null) {
                builder.customerEmail(customer.getEmail())
                       .customerName(customer.getDisplayName());
            }
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
