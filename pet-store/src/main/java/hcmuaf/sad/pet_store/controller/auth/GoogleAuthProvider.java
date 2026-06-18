package hcmuaf.sad.pet_store.controller.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import hcmuaf.sad.pet_store.config.GoogleOAuthConfig;
import hcmuaf.sad.pet_store.dto.auth.GoogleCredential;
import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.mapper.UserMapper;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleAuthProvider implements AuthProvider<GoogleCredential> {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthProvider() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(GoogleOAuthConfig.getClientId()))
                .build();
    }

    @Override
    public AuthenticatedUser authenticate(GoogleCredential credential) {
        // [3.1.4] Chuyển yêu cầu xác thực sang Google OAuth.
        GoogleIdToken.Payload payload = verifyGoogleToken(credential.getIdToken());

        // [3.1.6] Trả về định danh Google hợp lệ (googleSub)
        String googleSub = payload.getSubject();
        String email = payload.getEmail();
        String displayName = (String) payload.get("name");

        // [3.1.7] Tìm tài khoản liên kết Google
        User user = resolveCustomerByGoogleIdentity(googleSub, email, displayName);

        // Trả về tài khoản đã xác thực
        return UserMapper.toAuthenticatedUser(user);
    }

    // [3.1.5] Xác thực Google account của người dùng
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null)
                throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED);
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED, e);
        }
    }

    private User resolveCustomerByGoogleIdentity(String googleSub, String email, String displayName) {
        UserCredential stored = UserCredential.findByProviderUserId(ProviderType.GOOGLE, googleSub);

        // [3.1.7] Tìm tài khoản Customer theo Google sub đã liên kết
        if (stored != null) {
            User user = User.findActiveByUserCode(stored.getUserCode());
            // credential còn nhưng user bị xoá/inactive → lỗi nghiệp vụ
            if (user == null) throw new BusinessException(ErrorCode.GOOGLE_ACCESS_DENIED);
            return user;
        }

        // AF1: email chưa tồn tại → tạo tài khoản mới
        if (!User.existsByEmail(email)) {
            return createCustomerWithGoogleCredential(googleSub, email, displayName);
        }

        // EF2/EF3: email đã tồn tại nhưng chưa liên kết Google (hoặc là Admin) → cùng message chung
        throw new BusinessException(ErrorCode.GOOGLE_ACCESS_DENIED);
    }

    private User createCustomerWithGoogleCredential(String googleSub, String email, String displayName) {
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        User newUser = new User(userCode, email, displayName, UserRole.CUSTOMER);
        UserCredential googleCredential = new UserCredential(userCode, ProviderType.GOOGLE, googleSub, null);
        DBUtils.tx().executeWithoutResult(status -> {
            newUser.insert();
            googleCredential.insert();
        });
        return newUser;
    }
}
