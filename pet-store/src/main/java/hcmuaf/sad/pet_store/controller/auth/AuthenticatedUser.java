package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private String userCode;
    private String email;
    private String displayName;
    private UserRole role;
}
