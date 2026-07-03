package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerOrderHistoryItemDto {
    private Long orderId;
    private String orderCode;
    private LocalDateTime createdAt;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
}
