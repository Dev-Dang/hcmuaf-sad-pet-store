package hcmuaf.sad.pet_store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderCheckoutDto {
    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "Số điện thoại không đúng định dạng")
    private String receiverPhone;

    @NotBlank(message = "Vui lòng chọn địa chỉ hợp lệ từ gợi ý")
    private String shippingAddress;

    @NotBlank(message = "Thiếu mã không gian vị trí (placeId)")
    private String placeId;
}