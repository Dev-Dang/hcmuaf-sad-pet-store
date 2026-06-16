package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    private String displayName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Mật khẩu phải có ít nhất 1 chữ cái và 1 chữ số")
    private String password;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim().toLowerCase() : null;
    }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName != null) ? displayName.trim() : null;
    }
}
