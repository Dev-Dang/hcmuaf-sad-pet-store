package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "Vui lòng nhập tên nhà cung cấp.")
    @Size(max = 255, message = "Tên nhà cung cấp không được vượt quá 255 ký tự.")
    private String name;

    @Size(max = 255, message = "Tên người liên hệ không được vượt quá 255 ký tự.")
    private String contactPerson;

    @Pattern(regexp = "0\\d{9}", message = "Số điện thoại phải gồm 10 số và bắt đầu bằng 0.")
    private String phone;

    @Email(message = "Email không hợp lệ.")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự.")
    private String email;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự.")
    private String address;

    private boolean active = true;

    public void setName(String name) {
        this.name = trimToNull(name);
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = trimToNull(contactPerson);
    }

    public void setPhone(String phone) {
        this.phone = trimToNull(phone);
    }

    public void setEmail(String email) {
        String trimmed = trimToNull(email);
        this.email = trimmed == null ? null : trimmed.toLowerCase();
    }

    public void setAddress(String address) {
        this.address = trimToNull(address);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}