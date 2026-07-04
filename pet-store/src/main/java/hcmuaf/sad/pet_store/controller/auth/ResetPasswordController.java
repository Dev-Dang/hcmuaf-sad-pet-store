package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.request.NewPasswordRequest;
import hcmuaf.sad.pet_store.dto.request.ResetPasswordRequest;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.model.OtpChallenge;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.OtpPurpose;
import hcmuaf.sad.pet_store.model.enums.OtpTargetType;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.model.policy.OtpPolicy;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
public class ResetPasswordController {
    private static final String NEUTRAL_MESSAGE =
            "Nếu email hợp lệ, hệ thống đã gửi mã OTP. Vui lòng kiểm tra hộp thư.";

    private final EmailService emailService;
    private final String googleClientId;

    public ResetPasswordController(EmailService emailService,
                                   @Value("${google.client-id:}") String googleClientId) {
        this.emailService = emailService;
        this.googleClientId = googleClientId;
    }

    @GetMapping("/auth/forgot-password")
    public String showForgotPassword(Model model) {
        // [4.1.2] Hiển thị form nhập email đặt lại mật khẩu
        if (!model.containsAttribute("resetPasswordRequest")) {
            model.addAttribute("resetPasswordRequest", new ResetPasswordRequest());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String requestReset(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                               BindingResult bindingResult,
                               Model model) {
        // [4.1.4] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        // [4.1.5] Tìm tài khoản Customer active theo email
        User user = User.findActiveByEmail(request.getEmail());
        if (user == null || user.getRole() != UserRole.CUSTOMER) {
            return showNeutralOtpForm(model, "", neutralNextResendAt());
        }

        // [4.1.6] Kiểm tra tài khoản có phương thức EMAIL
        UserCredential resetCredential = UserCredential
                .findByUserCodeAndProvider(user.getUserCode(), ProviderType.EMAIL);
        if (resetCredential == null) {
            return showNeutralOtpForm(model, "", neutralNextResendAt());
        }

        // [4.1.7] Kiểm tra đã có phiên OTP đang hoạt động chưa
        OtpChallenge challenge = getOrCreateResetPasswordChallenge(user.getUserCode(), request.getEmail());

        // [4.1.12+4.1.14] Thông báo trung lập và hiển thị form OTP
        return showNeutralOtpForm(model, challenge.getChallengeId(), challenge.nextResendAt());
    }

    @GetMapping("/auth/reset-password")
    public String showNewPasswordForm(@RequestParam(value = "challengeId", required = false) String challengeId,
                                      Model model) {
        OtpChallenge challenge = OtpChallenge.findVerifiedByChallengeId(challengeId);
        if (challenge == null) {
            return "redirect:/auth/forgot-password";
        }
        // [4.1.17] Hiển thị form nhập mật khẩu mới
        NewPasswordRequest request = new NewPasswordRequest();
        request.setChallengeId(challenge.getChallengeId());
        model.addAttribute("newPasswordRequest", request);
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String updatePassword(@Valid @ModelAttribute("newPasswordRequest") NewPasswordRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        // [4.1.19] Kiểm tra mật khẩu mới hợp lệ
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        OtpChallenge challenge = OtpChallenge
                .findVerifiedByChallengeId(request.getChallengeId());
        if (challenge == null) {
            return "redirect:/auth/forgot-password";
        }

        UserCredential credential = UserCredential
                .findByUserCodeAndProvider(challenge.getUserCode(), ProviderType.EMAIL);
        if (credential == null) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR);
        }

        String secretHash = PasswordUtils.hash(request.getNewPassword());
        DBUtils.tx().executeWithoutResult(status -> {
            // [4.1.20] Cập nhật mật khẩu tài khoản Customer
            credential.updateSecretHash(secretHash);
            // [4.1.x BR11] Ghi nhận phiên đã hoàn tất
            challenge.markCompleted();
        });

        model.addAttribute("loginRequest", new hcmuaf.sad.pet_store.dto.request.LoginRequest());
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("success", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.");
        return "auth/login";
    }

    private OtpChallenge getOrCreateResetPasswordChallenge(String userCode, String email) {
        OtpChallenge challenge = OtpChallenge.findActiveByUserAndTarget(
                userCode, OtpPurpose.RESET_PASSWORD, OtpTargetType.EMAIL, email);
        if (challenge != null) {
            if (!challenge.isExpired()) {
                return challenge;
            }
            challenge.markExpired();
        }

        OtpChallenge newChallenge = OtpChallenge.createNewChallenge(
                userCode, OtpPurpose.RESET_PASSWORD, OtpTargetType.EMAIL, email);
        String otp = newChallenge.insertWithFirstOtp();
        // [4.1.10] Yêu cầu gửi OTP đến email Customer
        emailService.sendOtp(email, otp);
        return newChallenge;
    }

    private String showNeutralOtpForm(Model model, String challengeId, LocalDateTime nextResendAt) {
        model.addAttribute("otpRequest", new hcmuaf.sad.pet_store.dto.request.OtpRequest());
        model.addAttribute("challengeId", challengeId);
        model.addAttribute("resendRemainingSeconds", remainingSeconds(nextResendAt));
        model.addAttribute("resendRemainingText", remainingText(nextResendAt));
        model.addAttribute("message", NEUTRAL_MESSAGE);
        return "auth/reset-otp";
    }

    private LocalDateTime neutralNextResendAt() {
        return LocalDateTime.now().plusSeconds(OtpPolicy.RESEND_COOLDOWN_SECONDS);
    }

    private long remainingSeconds(LocalDateTime nextResendAt) {
        if (nextResendAt == null) {
            return 0;
        }
        return Math.max(0, Duration.between(LocalDateTime.now(), nextResendAt).toSeconds());
    }

    private String remainingText(LocalDateTime nextResendAt) {
        long remaining = remainingSeconds(nextResendAt);
        return String.format("%02d:%02d", remaining / 60, remaining % 60);
    }
}
