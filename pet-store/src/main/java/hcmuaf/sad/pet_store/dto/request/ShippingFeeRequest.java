package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShippingFeeRequest {
    
    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String fullName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String addressDetail;
    
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;

    // Helper method to format full address for Google Maps API
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", addressDetail, ward, district, city);
    }
}
