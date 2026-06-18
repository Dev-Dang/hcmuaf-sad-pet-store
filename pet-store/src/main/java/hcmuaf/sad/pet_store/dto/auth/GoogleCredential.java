package hcmuaf.sad.pet_store.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleCredential implements AuthCredential {
    private final String idToken;
}
