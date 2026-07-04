package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class BrevoEmailService implements EmailService {
    private static final String BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient = RestClient.create();
    private final String apiKey;
    private final String senderEmail;

    public BrevoEmailService(@Value("${brevo.api-key:}") String apiKey,
                             @Value("${brevo.sender-email:}") String senderEmail) {
        this.apiKey = blankToEmpty(apiKey);
        this.senderEmail = blankToEmpty(senderEmail);
    }

    @Override
    public void sendOtp(String email, String otp) {
        if (apiKey.isBlank() || senderEmail.isBlank()) {
            return;
        }
        try {
            restClient.post()
                    .uri(BREVO_SEND_EMAIL_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("api-key", apiKey)
                    .body(Map.of(
                            "sender", Map.of("email", senderEmail, "name", "Pet Store"),
                            "to", List.of(Map.of("email", email)),
                            "subject", "Mã OTP đặt lại mật khẩu",
                            "htmlContent", "<p>Mã OTP của bạn là <strong>" + otp
                                    + "</strong>. Mã có hiệu lực trong 10 phút.</p>"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
