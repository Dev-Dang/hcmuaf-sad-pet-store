package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerDetailDto {
    private String userCode;
    private String displayName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private int orderCount;
    private BigDecimal totalOrderValue;
}
