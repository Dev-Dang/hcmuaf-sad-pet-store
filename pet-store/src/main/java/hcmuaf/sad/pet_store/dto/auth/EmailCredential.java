package hcmuaf.sad.pet_store.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCredential implements AuthCredential {
    private final String email;
    private final String password;
}
