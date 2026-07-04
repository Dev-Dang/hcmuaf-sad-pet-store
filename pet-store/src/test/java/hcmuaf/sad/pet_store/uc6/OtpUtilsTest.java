package hcmuaf.sad.pet_store.uc6;

import hcmuaf.sad.pet_store.model.policy.OtpPolicy;
import hcmuaf.sad.pet_store.util.OtpUtils;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtpUtilsTest {
    @Test
    void generate_shouldReturnNumericOtpWithConfiguredLength() {
        String otp = OtpUtils.generate();

        assertThat(otp).hasSize(OtpPolicy.OTP_LENGTH);
        assertThat(otp).matches("\\d{" + OtpPolicy.OTP_LENGTH + "}");
    }

    @Test
    void verify_shouldUsePasswordHashMatching() {
        String hash = PasswordUtils.hash("123456");

        assertThat(OtpUtils.verify("123456", hash)).isTrue();
        assertThat(OtpUtils.verify("000000", hash)).isFalse();
    }
}
