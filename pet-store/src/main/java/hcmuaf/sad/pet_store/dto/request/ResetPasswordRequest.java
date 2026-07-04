package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    public void setEmail(String email) {
        this.email = (email != null) ? email.trim().toLowerCase() : null;
    }
}
