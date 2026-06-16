package hcmuaf.sad.pet_store.mapper;

import hcmuaf.sad.pet_store.controller.auth.AuthenticatedUser;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.enums.UserRole;

public class UserMapper {

    public static AuthenticatedUser toAuthenticatedUser(User user) {
        return new AuthenticatedUser(
                user.getUserCode(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    public static AuthenticatedUser toAuthenticatedUser(String userCode, String email, String displayName, UserRole role) {
        return new AuthenticatedUser(userCode, email, displayName, role);
    }
}
