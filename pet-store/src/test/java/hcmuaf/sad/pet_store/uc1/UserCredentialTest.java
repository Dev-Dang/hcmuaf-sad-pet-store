package hcmuaf.sad.pet_store.uc1;

import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "uc1"})
@Transactional
class UserCredentialTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // [1.1.6] Tạo EMAIL credential — verify userId, provider, secretHash, providerUserId=null
    @Test
    void insert_emailCredential_shouldPersistCorrectly() {
        String userCode = UUID.randomUUID().toString();
        String secretHash = "hashed_password_value";

        new UserCredential(userCode, ProviderType.EMAIL, null, secretHash).insert();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM user_credential WHERE user_code = ?", userCode);

        assertThat(row.get("user_code")).isEqualTo(userCode);
        assertThat(row.get("provider")).isEqualTo("EMAIL");
        assertThat(row.get("secret_hash")).isEqualTo(secretHash);
        assertThat(row.get("provider_user_id")).isNull();
    }
}
