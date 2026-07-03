package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Vui lòng nhập họ tên người nhận.")
    @Size(max = 255, message = "Họ tên người nhận không được vượt quá 255 ký tự.")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "0\\d{9}", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng 0.")
    private String phone;

    @NotBlank(message = "Vui lòng chọn địa chỉ từ danh sách gợi ý.")
    private String placeId;

    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự.")
    private String addressDetail;

    private String fullAddress;

    public void setRecipientName(String recipientName) {
        this.recipientName = trimToNull(recipientName);
    }

    public void setPhone(String phone) {
        this.phone = trimToNull(phone);
    }

    public void setPlaceId(String placeId) {
        this.placeId = trimToNull(placeId);
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = trimToNull(addressDetail);
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = trimToNull(fullAddress);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
