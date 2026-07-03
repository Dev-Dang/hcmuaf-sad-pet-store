package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VariantResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private int availableStock;
    private String status;
}
