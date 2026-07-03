package hcmuaf.sad.pet_store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentFormDto {
    @NotBlank(message = "Mã đơn hàng không hợp lệ")
    private String orderCode;

    @NotNull(message = "Số tiền thanh toán không được thiếu")
    private Double amount;

    @NotBlank(message = "Chưa chọn phương thức thanh toán")
    private String paymentType;

    private Double cashTendered;

    private String checkName;
    private String checkBankId;

    private String creditNumber;
    private String creditType;
    private String creditExpDate;
}