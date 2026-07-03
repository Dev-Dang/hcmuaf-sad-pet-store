package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private String productName;
    private String variantName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
