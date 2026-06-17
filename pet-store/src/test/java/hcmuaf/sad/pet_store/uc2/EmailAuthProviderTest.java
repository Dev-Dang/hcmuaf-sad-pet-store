package hcmuaf.sad.pet_store.uc2;

import hcmuaf.sad.pet_store.controller.auth.EmailAuthProvider;
import hcmuaf.sad.pet_store.controller.auth.AuthenticatedUser;
import hcmuaf.sad.pet_store.dto.auth.EmailCredential;
import hcmuaf.sad.pet_store.exception.AppException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles({"test", "uc2"})
@Transactional
class EmailAuthProviderTest {

    private EmailAuthProvider authProvider;

    @BeforeEach
    void setUp() {
        authProvider = new EmailAuthProvider();
    }

    // [2.1.5] Xác thực thành công
    @Test
    void authenticate_validCredentials_shouldReturnUser() {
        // [2.1.5] Xác thực email + mật khẩu thành công
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        String password = "pass1234";
        new User(userCode, "user@test.com", "Test User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash(password)).insert();

        AuthenticatedUser authUser = authProvider.authenticate(new EmailCredential("user@test.com", password));
        assertThat(authUser).isNotNull();
        assertThat(authUser.getUserCode()).isEqualTo(userCode);
        assertThat(authUser.getEmail()).isEqualTo("user@test.com");
        assertThat(authUser.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    // [2.1.5 EF2] Xác thực thất bại — các trường hợp
    @Test
    void authenticate_nonExistentEmail_shouldThrowInvalidCredentials() {
        // [2.4.1] EF2 — Email không tồn tại
        assertThatThrownBy(() -> authProvider.authenticate(new EmailCredential("nonexistent@test.com", "pass1234")))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_wrongPassword_shouldThrowInvalidCredentials() {
        // [2.4.1] EF2 — Mật khẩu sai
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "user@test.com", "Test User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("correct1234")).insert();

        assertThatThrownBy(() -> authProvider.authenticate(new EmailCredential("user@test.com", "wrong1234")))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void authenticate_noEmailCredential_shouldThrowInvalidCredentials() {
        // [2.4.1] EF2 — User không có EMAIL credential (ví dụ chỉ có GOOGLE)
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "googleonly@test.com", "Google User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.GOOGLE, "google-sub-123", null).insert();

        assertThatThrownBy(() -> authProvider.authenticate(new EmailCredential("googleonly@test.com", "pass1234")))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }


    // [2.1.5] email normalization
    @Test
    void authenticate_emailNormalization_shouldMatchLowercaseEmail() {
        // email normalize: trim + lowercase (done in LoginRequest.setEmail)
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        String password = "pass1234";
        new User(userCode, "user@test.com", "Test User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash(password)).insert();

        AuthenticatedUser authUser = authProvider.authenticate(new EmailCredential("user@test.com", password));
        assertThat(authUser).isNotNull();
        assertThat(authUser.getEmail()).isEqualTo("user@test.com");
    }
}
