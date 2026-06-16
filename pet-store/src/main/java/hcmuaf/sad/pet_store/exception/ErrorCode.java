package hcmuaf.sad.pet_store.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS(409, "Email này đã được đăng ký. Vui lòng dùng email khác hoặc đăng nhập."),
    INVALID_CREDENTIALS(401, "Email hoặc mật khẩu không đúng."),
    SESSION_EXPIRED(401, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."),
    OTP_INVALID(400, "Mã OTP không đúng. Vui lòng kiểm tra lại."),
    OTP_EXPIRED(400, "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới."),
    OTP_MAX_ATTEMPT(429, "Đã vượt quá số lần thử OTP."),
    OTP_MAX_RESEND(429, "Đã vượt quá số lần gửi lại OTP."),
    OTP_COOLDOWN(429, "Vui lòng chờ trước khi yêu cầu gửi lại OTP."),
    ADDRESS_NOT_FOUND(404, "Địa chỉ không tồn tại."),
    ADDRESS_IS_DEFAULT(400, "Không thể xóa địa chỉ mặc định."),
    ADDRESS_COORDS_MISSING(400, "Không tìm thấy tọa độ địa chỉ."),
    GOOGLE_PLACES_ERROR(502, "Không thể kết nối dịch vụ bản đồ. Vui lòng thử lại."),
    PERMISSION_DENIED(403, "Bạn không có quyền truy cập trang này."),
    SYSTEM_ERROR(500, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");

    private final int httpStatus;
    private final String message;

    ErrorCode(int httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
