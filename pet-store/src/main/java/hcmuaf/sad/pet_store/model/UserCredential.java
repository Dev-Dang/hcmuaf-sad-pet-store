package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

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
        try {
            DBUtils.jdbc().update("""
                    INSERT INTO user_credential (user_code, provider, provider_user_id, secret_hash, linked_at)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    userCode, provider.name(), providerUserId, secretHash, LocalDateTime.now());
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    private static final RowMapper<UserCredential> ROW_MAPPER = (rs, rowNum) -> {
        UserCredential c = new UserCredential(
                rs.getString("user_code"),
                ProviderType.valueOf(rs.getString("provider")),
                rs.getString("provider_user_id"),
                rs.getString("secret_hash")
        );
        c.setId(rs.getLong("id"));
        c.setLinkedAt(rs.getObject("linked_at", LocalDateTime.class));
        return c;
    };

    // ─── UC-2/4 ───────────────────────────────────────────────────────────────

    public static UserCredential findByUserCodeAndProvider(String userCode, ProviderType provider) {
        try {
            List<UserCredential> results = DBUtils.jdbc().query(
                    "SELECT * FROM user_credential WHERE user_code = ? AND provider = ?",
                    ROW_MAPPER, userCode, provider.name());
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static UserCredential findByProviderUserId(ProviderType provider, String providerUserId) {
        try {
            List<UserCredential> results = DBUtils.jdbc().query(
                    "SELECT * FROM user_credential WHERE provider = ? AND provider_user_id = ?",
                    ROW_MAPPER, provider.name(), providerUserId);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static List<UserCredential> findByUserCode(String userCode) {
        try {
            return DBUtils.jdbc().query(
                    "SELECT * FROM user_credential WHERE user_code = ?",
                    ROW_MAPPER, userCode);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void updateSecretHash(String hash) {
        try {
            DBUtils.jdbc().update(
                    "UPDATE user_credential SET secret_hash = ? WHERE id = ?",
                    hash, id);
            this.secretHash = hash;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void delete() {
        // TODO: hủy liên kết provider
        throw new UnsupportedOperationException("TODO: delete credential");
    }
}
