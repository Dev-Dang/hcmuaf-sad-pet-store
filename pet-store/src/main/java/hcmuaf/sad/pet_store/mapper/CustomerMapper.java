package hcmuaf.sad.pet_store.mapper;

import hcmuaf.sad.pet_store.dto.response.CustomerDetailDto;
import hcmuaf.sad.pet_store.dto.response.CustomerListItemDto;
import hcmuaf.sad.pet_store.dto.response.CustomerOrderHistoryItemDto;
import hcmuaf.sad.pet_store.model.Order;
import hcmuaf.sad.pet_store.model.OrderSummary;
import hcmuaf.sad.pet_store.model.ShippingAddress;
import hcmuaf.sad.pet_store.model.User;

import java.util.List;

public class CustomerMapper {

    public static List<CustomerListItemDto> toCustomerListDto(List<User> customers) {
        return customers.stream()
                .map(CustomerMapper::toCustomerListItemDto)
                .toList();
    }

    public static CustomerDetailDto toCustomerDetailDto(User customer,
                                                        ShippingAddress address,
                                                        OrderSummary summary) {
        CustomerDetailDto dto = new CustomerDetailDto();
        dto.setUserCode(customer.getUserCode());
        dto.setDisplayName(customer.getDisplayName());
        dto.setEmail(customer.getEmail());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setPhone(address == null ? null : address.getPhone());
        dto.setAddress(address == null ? null : address.getFullAddress());
        dto.setOrderCount(summary.orderCount());
        dto.setTotalOrderValue(summary.totalValue());
        return dto;
    }

    public static List<CustomerOrderHistoryItemDto> toOrderHistoryDto(List<Order> orders) {
        return orders.stream()
                .map(CustomerMapper::toOrderHistoryItemDto)
                .toList();
    }

    private static CustomerListItemDto toCustomerListItemDto(User customer) {
        CustomerListItemDto dto = new CustomerListItemDto();
        dto.setUserCode(customer.getUserCode());
        dto.setDisplayName(customer.getDisplayName());
        dto.setEmail(customer.getEmail());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }

    private static CustomerOrderHistoryItemDto toOrderHistoryItemDto(Order order) {
        CustomerOrderHistoryItemDto dto = new CustomerOrderHistoryItemDto();
        dto.setOrderCode(order.getOrderCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        return dto;
    }
}
