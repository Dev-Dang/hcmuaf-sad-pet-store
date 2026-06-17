package hcmuaf.sad.pet_store.uc2;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "uc2"})
@Transactional
class UserCredentialTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // [2.1.5] Lấy EMAIL credential của User
    @Test
    void findByUserCodeAndProvider_existingCredential_shouldReturnCredential() {
        String userCode = UUID.randomUUID().toString();
        String secretHash = PasswordUtils.hash("pass1234");
        new UserCredential(userCode, ProviderType.EMAIL, null, secretHash).insert();

        UserCredential credential = UserCredential.findByUserCodeAndProvider(userCode, ProviderType.EMAIL);
        assertThat(credential).isNotNull();
        assertThat(credential.getUserCode()).isEqualTo(userCode);
        assertThat(credential.getProvider()).isEqualTo(ProviderType.EMAIL);
        assertThat(credential.getProviderUserId()).isNull();
        assertThat(credential.getSecretHash()).isEqualTo(secretHash);
    }

    @Test
    void findByUserCodeAndProvider_nonExistent_shouldReturnNull() {
        // [2.1.5] Không tìm thấy EMAIL credential → return null
        String userCode = UUID.randomUUID().toString();
        UserCredential credential = UserCredential.findByUserCodeAndProvider(userCode, ProviderType.EMAIL);
        assertThat(credential).isNull();
    }

    @Test
    void findByUserCodeAndProvider_wrongProvider_shouldReturnNull() {
        // [2.1.5] User chỉ có GOOGLE credential, tìm EMAIL → return null
        String userCode = UUID.randomUUID().toString();
        new UserCredential(userCode, ProviderType.GOOGLE, "google-sub-123", null).insert();

        UserCredential credential = UserCredential.findByUserCodeAndProvider(userCode, ProviderType.EMAIL);
        assertThat(credential).isNull();
    }
}
