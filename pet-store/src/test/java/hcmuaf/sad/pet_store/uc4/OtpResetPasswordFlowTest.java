package hcmuaf.sad.pet_store.uc4;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OtpResetPasswordFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM otp_records");
        jdbc.update("DELETE FROM otp_challenges");
        jdbc.update("DELETE FROM user_credential WHERE user_code LIKE 'KHG-UC4-%'");
        jdbc.update("DELETE FROM users WHERE user_code LIKE 'KHG-UC4-%'");
    }

    @Test
    void requestReset_existingCustomer_shouldCreateChallengeAndOtpRecord() throws Exception {
        String userCode = "KHG-UC4-001";
        seedCustomer(userCode, "customer.uc4@test.com", "oldpass123");

        MvcResult result = mockMvc.perform(post("/auth/forgot-password")
                        .param("email", " CUSTOMER.UC4@Test.com "))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-otp"))
                .andExpect(model().attributeExists("challengeId"))
                .andExpect(model().attributeExists("resendRemainingSeconds"))
                .andExpect(model().attributeExists("resendRemainingText"))
                .andReturn();

        String challengeId = (String) result.getModelAndView().getModel().get("challengeId");
        Integer challengeCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM otp_challenges WHERE user_code = ?", Integer.class, userCode);
        Integer recordCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM otp_records WHERE challenge_id = ?", Integer.class, challengeId);
        assertThat(challengeId).isNotBlank();
        assertThat(challengeCount).isEqualTo(1);
        assertThat(recordCount).isEqualTo(1);
    }

    @Test
    void verify_wrongOtp_shouldIncrementAttemptAndReturnOtpForm() throws Exception {
        String challengeId = seedPendingChallenge("KHG-UC4-002", "wrong-otp.uc4@test.com", "123456");

        mockMvc.perform(post("/auth/otp/verify")
                        .param("challengeId", challengeId)
                        .param("otp", "000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-otp"))
                .andExpect(model().attributeExists("error"));

        Integer attemptCount = jdbc.queryForObject(
                "SELECT attempt_count FROM otp_records WHERE challenge_id = ?", Integer.class, challengeId);
        assertThat(attemptCount).isEqualTo(1);
    }

    @Test
    void verify_validOtp_shouldMarkUsedAndVerifiedThenRedirectResetPassword() throws Exception {
        String challengeId = seedPendingChallenge("KHG-UC4-003", "valid-otp.uc4@test.com", "123456");

        mockMvc.perform(post("/auth/otp/verify")
                        .param("challengeId", challengeId)
                        .param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/auth/reset-password?challengeId=*"));

        Map<String, Object> challenge = jdbc.queryForMap(
                "SELECT status, verified_otp_record_id FROM otp_challenges WHERE challenge_id = ?", challengeId);
        String recordStatus = jdbc.queryForObject(
                "SELECT status FROM otp_records WHERE challenge_id = ?", String.class, challengeId);

        assertThat(challenge.get("status")).isEqualTo("VERIFIED");
        assertThat(challenge.get("verified_otp_record_id")).isNotNull();
        assertThat(recordStatus).isEqualTo("USED");
    }

    @Test
    void updatePassword_verifiedChallenge_shouldUpdateCredentialAndCompleteChallenge() throws Exception {
        String userCode = "KHG-UC4-004";
        seedCustomer(userCode, "reset.uc4@test.com", "oldpass123");
        String challengeId = seedVerifiedChallenge(userCode, "reset.uc4@test.com");

        mockMvc.perform(post("/auth/reset-password")
                        .param("challengeId", challengeId)
                        .param("newPassword", "newpass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("success"));

        String hash = jdbc.queryForObject(
                "SELECT secret_hash FROM user_credential WHERE user_code = ? AND provider = 'EMAIL'",
                String.class, userCode);
        String status = jdbc.queryForObject(
                "SELECT status FROM otp_challenges WHERE challenge_id = ?", String.class, challengeId);

        assertThat(PasswordUtils.verify("newpass123", hash)).isTrue();
        assertThat(status).isEqualTo("COMPLETED");
    }

    private void seedCustomer(String userCode, String email, String password) {
        new User(userCode, email, "Test Customer", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash(password)).insert();
    }

    private String seedPendingChallenge(String userCode, String email, String otp) {
        seedCustomer(userCode, email, "oldpass123");
        String challengeId = "challenge-" + System.nanoTime();
        LocalDateTime now = LocalDateTime.now();
        jdbc.update("""
                INSERT INTO otp_challenges
                (challenge_id, purpose, target_type, target_value, user_code, status,
                 resend_count, last_sent_at, expires_at, created_at)
                VALUES (?, 'RESET_PASSWORD', 'EMAIL', ?, ?, 'PENDING', 0, ?, ?, ?)
                """, challengeId, email, userCode, now, now.plusMinutes(30), now);
        jdbc.update("""
                INSERT INTO otp_records
                (challenge_id, otp_hash, attempt_count, status, sent_at, expires_at, created_at)
                VALUES (?, ?, 0, 'ACTIVE', ?, ?, ?)
                """, challengeId, PasswordUtils.hash(otp), now, now.plusMinutes(10), now);
        return challengeId;
    }

    private String seedVerifiedChallenge(String userCode, String email) {
        String challengeId = seedPendingChallenge(userCode, email, "123456");
        Long recordId = jdbc.queryForObject("SELECT id FROM otp_records WHERE challenge_id = ?", Long.class, challengeId);
        jdbc.update("UPDATE otp_records SET status = 'USED', used_at = ? WHERE id = ?", LocalDateTime.now(), recordId);
        jdbc.update("""
                UPDATE otp_challenges
                SET status = 'VERIFIED', verified_otp_record_id = ?, verified_at = ?
                WHERE challenge_id = ?
                """, recordId, LocalDateTime.now(), challengeId);
        return challengeId;
    }
}
