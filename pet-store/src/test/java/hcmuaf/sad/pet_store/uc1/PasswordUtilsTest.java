package hcmuaf.sad.pet_store.uc1;

import hcmuaf.sad.pet_store.util.PasswordUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilsTest {

    @Test
    void hash_shouldReturnEncodedString() {
        String raw = "mypassword123";
        String hash = PasswordUtils.hash(raw);
        assertThat(hash).isNotNull().isNotEqualTo(raw);
    }

    @Test
    void hash_thenVerify_shouldMatch() {
        String raw = "mypassword123";
        String hash = PasswordUtils.hash(raw);
        assertThat(PasswordUtils.verify(raw, hash)).isTrue();
    }

    @Test
    void verify_wrongPassword_shouldReturnFalse() {
        String hash = PasswordUtils.hash("correctpassword");
        assertThat(PasswordUtils.verify("wrongpassword", hash)).isFalse();
    }

    @Test
    void hash_samePlaintext_shouldProduceDifferentHashes() {
        // Argon2 dùng salt ngẫu nhiên — 2 hash cùng password phải khác nhau
        String hash1 = PasswordUtils.hash("samepassword");
        String hash2 = PasswordUtils.hash("samepassword");
        assertThat(hash1).isNotEqualTo(hash2);
        // nhưng cả 2 đều verify được
        assertThat(PasswordUtils.verify("samepassword", hash1)).isTrue();
        assertThat(PasswordUtils.verify("samepassword", hash2)).isTrue();
    }
}
