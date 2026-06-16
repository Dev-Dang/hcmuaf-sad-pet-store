package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserCredential {
    private Long id;
    private String userCode;
    private ProviderType provider;
    private String providerUserId;
    private String secretHash;
    private LocalDateTime linkedAt;

    public UserCredential(String userCode, ProviderType provider, String providerUserId, String secretHash) {
        this.userCode = userCode;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.secretHash = secretHash;
    }

    // ─── UC-1 ─────────────────────────────────────────────────────────────────

    public void insert() {
        DBUtils.jdbc().update("""
                    INSERT INTO user_credential (user_code, provider, provider_user_id, secret_hash, linked_at)
                    VALUES (?, ?, ?, ?, ?)
                """,
                userCode, provider.name(), providerUserId, secretHash, LocalDateTime.now());
    }

    // ─── TODO: UC-2/3/4 ───────────────────────────────────────────────────────

    public static UserCredential findByUserCodeAndProvider(String userCode, ProviderType provider) {
        // TODO: UC-2/4
        throw new UnsupportedOperationException("TODO: UC-2/4");
    }

    public static UserCredential findByProviderUserId(ProviderType provider, String providerUserId) {
        // TODO: UC-3
        throw new UnsupportedOperationException("TODO: UC-3");
    }

    public static java.util.List<UserCredential> findByUserCode(String userCode) {
        // TODO: UC-3
        throw new UnsupportedOperationException("TODO: UC-3");
    }

    public void updateSecretHash(String hash) {
        // TODO: UC-4
        throw new UnsupportedOperationException("TODO: UC-4");
    }

    public void delete() {
        // TODO: hủy liên kết provider
        throw new UnsupportedOperationException("TODO: delete credential");
    }
}
