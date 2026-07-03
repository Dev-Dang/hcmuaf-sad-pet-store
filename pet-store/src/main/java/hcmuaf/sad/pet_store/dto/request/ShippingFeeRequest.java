package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShippingFeeRequest {
    
    private String fullName;
    
    private String phone;
    
    private String addressDetail;
    private String ward;
    private String district;
    private String city;
    
    private String fullAddress;

    // Ghép địa chỉ đầy đủ để gửi tới Goong Maps Geocode API
    public String getResolvedFullAddress() {
        if (fullAddress != null && !fullAddress.isBlank()) {
            return fullAddress;
        }
        return String.format("%s, %s, %s, %s", addressDetail, ward, district, city);
    }
}
