package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.auth.GoogleCredential;

public class GoogleAuthProvider implements AuthProvider<GoogleCredential> {

    @Override
    public AuthenticatedUser authenticate(GoogleCredential credential) {
        // TODO: UC-3 — đổi authorization code lấy token, verify, lookup user
        throw new UnsupportedOperationException("TODO: UC-3");
    }
}
