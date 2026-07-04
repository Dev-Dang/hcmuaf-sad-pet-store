package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.request.OtpRequest;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.model.OtpChallenge;
import hcmuaf.sad.pet_store.model.OtpRecord;
import hcmuaf.sad.pet_store.model.policy.OtpPolicy;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.util.OtpUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;

@Controller
public class OtpController {
    private final EmailService emailService;

    public OtpController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/auth/otp/verify")
    public String verify(@Valid @ModelAttribute("otpRequest") OtpRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        // [6.1.3] Kiểm tra dữ liệu hợp lệ
        if (bindingResult.hasErrors()) {
            return otpForm(model, request.getChallengeId(), null, "Mã OTP không hợp lệ. Vui lòng nhập lại.");
        }

        // [6.1.4] Xác định phiên xác thực OTP hiện tại
        OtpChallenge challenge = OtpChallenge.findActiveByChallengeId(request.getChallengeId());
        if (challenge == null || challenge.isExpired()) {
            markExpiredIfPossible(challenge);
            redirectAttributes.addFlashAttribute("error", ErrorCode.OTP_CHALLENGE_INVALID.getMessage());
            return redirectToStart(challenge);
        }

        // [6.1.6] Lấy mã OTP hiện hành của phiên
        OtpRecord record = OtpRecord.findActiveByChallengeId(challenge.getChallengeId());
        if (record == null || record.isExpired()) {
            return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(),
                    "Mã OTP không còn hiệu lực. Vui lòng yêu cầu mã OTP mới.");
        }
        if (record.isMaxAttemptReached()) {
            return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(),
                    "Bạn đã thử quá số lần cho phép. Vui lòng yêu cầu mã OTP mới.");
        }

        // [6.1.7] So khớp OTP
        if (!OtpUtils.verify(request.getOtp(), record.getOtpHash())) {
            // [6.7.1] Ghi nhận thêm một lần thử sai
            record.incrementAttemptCount();
            return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(),
                    ErrorCode.OTP_MISMATCH.getMessage());
        }

        DBUtils.tx().executeWithoutResult(status -> {
            // [6.1.8] Ghi nhận mã OTP đã sử dụng
            record.markUsed();
            // [6.1.9] Ghi nhận phiên đã xác minh
            challenge.markVerified(record.getId());
        });

        // [6.1.10] Định tuyến theo challenge.purpose
        redirectAttributes.addAttribute("challengeId", challenge.getChallengeId());
        return "redirect:/auth/reset-password";
    }

    @PostMapping("/auth/otp/resend")
    public String resend(@RequestParam(value = "challengeId", required = false) String challengeId, Model model,
                         RedirectAttributes redirectAttributes) {
        // [6.2.1] Xác định phiên xác thực OTP hiện tại
        OtpChallenge challenge = OtpChallenge.findActiveByChallengeId(challengeId);
        if (challengeId == null || challengeId.isBlank()) {
            return otpForm(model, "", neutralNextResendAt(),
                    "Nếu email hợp lệ, hệ thống đã gửi mã OTP. Vui lòng kiểm tra hộp thư.");
        }
        if (challenge == null || challenge.isExpired()) {
            markExpiredIfPossible(challenge);
            redirectAttributes.addFlashAttribute("error", ErrorCode.OTP_CHALLENGE_INVALID.getMessage());
            return redirectToStart(challenge);
        }

        // [6.2.3] Kiểm tra số lần gửi lại đã vượt giới hạn
        if (challenge.isMaxResendReached()) {
            return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(),
                    "Bạn đã hết lượt gửi lại OTP. Vui lòng nhập mã OTP gần nhất đã nhận.");
        }

        // [6.2.3] Kiểm tra chưa qua thời gian chờ giữa 2 lần gửi
        if (challenge.isCooldownActive()) {
            return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(),
                    ErrorCode.OTP_RESEND_COOLDOWN.getMessage());
        }

        String otp = OtpUtils.generate();
        OtpRecord record = OtpRecord.createActiveForChallenge(challenge.getChallengeId(), otp);

        DBUtils.tx().executeWithoutResult(status -> {
            OtpRecord activeRecord = OtpRecord.findActiveByChallengeId(challenge.getChallengeId());
            if (activeRecord != null) {
                // [6.2.4] Vô hiệu hóa mã OTP hiện hành
                activeRecord.invalidate();
            }
            // [6.2.5] Lưu mã OTP mới cho cùng phiên
            record.insert();
            // [6.2.5] Cập nhật số lần gửi lại và thời điểm gửi
            challenge.incrementResendCount();
        });

        // [6.2.6] Yêu cầu gửi mã OTP mới đến email Customer
        emailService.sendOtp(challenge.getTargetValue(), otp);

        // [6.2.9] Hiển thị form OTP với thời gian chờ còn lại
        return otpForm(model, challenge.getChallengeId(), challenge.nextResendAt(), "Mã OTP mới đã được gửi.");
    }

    private String otpForm(Model model, String challengeId, LocalDateTime nextResendAt, String error) {
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setChallengeId(challengeId);
        model.addAttribute("otpRequest", otpRequest);
        model.addAttribute("challengeId", challengeId);
        model.addAttribute("resendRemainingSeconds", remainingSeconds(nextResendAt));
        model.addAttribute("resendRemainingText", remainingText(nextResendAt));
        model.addAttribute("error", error);
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

    private void markExpiredIfPossible(OtpChallenge challenge) {
        if (challenge != null) {
            // [6.1.5] Đánh dấu phiên quá hạn
            challenge.markExpired();
        }
    }

    private String redirectToStart(OtpChallenge challenge) {
        return "redirect:/auth/forgot-password";
    }
}
