package hcmuaf.sad.pet_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank(message = "Phiên xác thực không hợp lệ")
    private String challengeId;

    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP không hợp lệ. Vui lòng nhập lại.")
    private String otp;
}
