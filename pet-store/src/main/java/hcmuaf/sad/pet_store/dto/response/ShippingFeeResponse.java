package hcmuaf.sad.pet_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeResponse {
    
    private BigDecimal fee;
    private Integer distanceKm;
    private boolean isFallback;
    
}
