package hcmuaf.sad.pet_store.controller.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MapPlaceResult {
    private String placeId;
    private String fullAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
