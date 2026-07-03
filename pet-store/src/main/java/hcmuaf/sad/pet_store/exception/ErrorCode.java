package hcmuaf.sad.pet_store.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS("Email này đã được đăng ký. Vui lòng dùng email khác hoặc đăng nhập."),
    INVALID_CREDENTIALS("Email hoặc mật khẩu không đúng."),
    SESSION_EXPIRED("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."),
    OTP_INVALID("Mã OTP không đúng. Vui lòng kiểm tra lại."),
    OTP_EXPIRED("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới."),
    OTP_MAX_ATTEMPT("Đã vượt quá số lần thử OTP."),
    OTP_MAX_RESEND("Đã vượt quá số lần gửi lại OTP."),
    OTP_COOLDOWN("Vui lòng chờ trước khi yêu cầu gửi lại OTP."),
    ADDRESS_NOT_FOUND("Địa chỉ không tồn tại."),
    ADDRESS_IS_DEFAULT("Không thể xóa địa chỉ mặc định."),
    ADDRESS_COORDS_MISSING("Không tìm thấy tọa độ địa chỉ."),
    CUSTOMER_NOT_FOUND("Khách hàng không tồn tại."),
    GOONG_MAPS_ERROR("Không thể kết nối dịch vụ bản đồ. Vui lòng thử lại."),
    GOOGLE_AUTH_FAILED("Đăng nhập bằng Google không thành công. Vui lòng thử lại."),
    GOOGLE_ACCESS_DENIED("Không thể đăng nhập bằng Google với tài khoản này. Vui lòng thử phương thức đăng nhập khác."),
    PERMISSION_DENIED("Bạn không có quyền truy cập trang này."),
    SYSTEM_ERROR("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
