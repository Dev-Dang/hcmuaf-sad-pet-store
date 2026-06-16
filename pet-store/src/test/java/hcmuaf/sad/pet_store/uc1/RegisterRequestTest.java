package hcmuaf.sad.pet_store.uc1;

import hcmuaf.sad.pet_store.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void setEmail_shouldTrimAndLowercase() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("  Test@GMAIL.COM  ");
        assertThat(req.getEmail()).isEqualTo("test@gmail.com");
    }

    @Test
    void setEmail_withNull_shouldSetNull() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(null);
        assertThat(req.getEmail()).isNull();
    }

    @Test
    void setDisplayName_shouldTrim() {
        RegisterRequest req = new RegisterRequest();
        req.setDisplayName("  Nguyen Van A  ");
        assertThat(req.getDisplayName()).isEqualTo("Nguyen Van A");
    }

    @Test
    void setDisplayName_withNull_shouldSetNull() {
        RegisterRequest req = new RegisterRequest();
        req.setDisplayName(null);
        assertThat(req.getDisplayName()).isNull();
    }
}
