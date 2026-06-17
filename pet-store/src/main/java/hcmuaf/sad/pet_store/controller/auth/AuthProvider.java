package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.auth.AuthCredential;

public interface AuthProvider<C extends AuthCredential> {
    AuthenticatedUser authenticate(C credential);
}
