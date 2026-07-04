package hcmuaf.sad.pet_store.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import hcmuaf.sad.pet_store.controller.address.GoongMapProvider;
import hcmuaf.sad.pet_store.controller.auth.BrevoEmailService;
import hcmuaf.sad.pet_store.controller.auth.EmailService;
import hcmuaf.sad.pet_store.controller.auth.GoogleAuthProvider;
import hcmuaf.sad.pet_store.exception.SystemException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigValuePojoTest {

    @Test
    void emailService_blankApiKeyAndSender_shouldSkipSending() {
        EmailService service = new BrevoEmailService("", "");

        assertThatCode(() -> service.sendOtp("customer@example.com", "123456"))
                .doesNotThrowAnyException();
    }

    @Test
    void goongMap_missingApiKey_shouldFailBeforeCallingApi() {
        GoongMapProvider goongMap = new GoongMapProvider("", "https://rsapi.goong.io", "v2");

        assertThatThrownBy(() -> goongMap.autocomplete("Linh Trung"))
                .isInstanceOf(SystemException.class);
    }

    @Test
    void goongMap_missingBaseUrl_shouldFailBeforeCallingApi() {
        GoongMapProvider goongMap = new GoongMapProvider("test-api-key", "", "v2");

        assertThatThrownBy(() -> goongMap.autocomplete("Linh Trung"))
                .isInstanceOf(SystemException.class);
    }

    @Test
    void googleAuthProvider_shouldUseConstructorClientIdAsAudience() {
        GoogleAuthProvider provider = new GoogleAuthProvider("test-google-client-id");
        GoogleIdTokenVerifier verifier = (GoogleIdTokenVerifier) ReflectionTestUtils.getField(provider, "verifier");

        assertThat(verifier).isNotNull();
        Collection<String> audience = verifier.getAudience();
        assertThat(audience).containsExactly("test-google-client-id");
    }
}
