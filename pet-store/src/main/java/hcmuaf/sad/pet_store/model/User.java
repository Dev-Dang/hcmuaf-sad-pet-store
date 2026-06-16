package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.model.base.BaseEntity;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class User extends BaseEntity {
    private String userCode;
    private String email;
    private String displayName;
    private UserRole role;

    public User(String userCode, String email, String displayName, UserRole role) {
        this.userCode = userCode;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }

    // ─── UC-1 ─────────────────────────────────────────────────────────────────

    public static boolean existsByEmail(String email) {
        Integer count = DBUtils.jdbc().queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND is_current = true",
                Integer.class, email);
        return count != null && count > 0;
    }

    public void insert() {
        LocalDateTime now = LocalDateTime.now();
        DBUtils.jdbc().update("""
                INSERT INTO users (user_code, email, display_name, role, effective_from, is_current, is_deleted, created_at)
                VALUES (?, ?, ?, ?, ?, true, false, ?)
                """,
                userCode, email, displayName, role.name(), now, now);
    }

    // ─── TODO: UC-2/3/4 ───────────────────────────────────────────────────────

    public static User findActiveByEmail(String email) {
        // TODO: UC-2/3/4
        throw new UnsupportedOperationException("TODO: UC-2/3/4");
    }

    public static User findActiveByUserId(String userId) {
        // TODO: UC-2/3
        throw new UnsupportedOperationException("TODO: UC-2/3");
    }

    public void updateDisplayName(String name) {
        // TODO: SCD Type 2 update
        throw new UnsupportedOperationException("TODO: SCD update");
    }

    @Override
    public void softDelete() {
        // TODO: SCD tombstone
        throw new UnsupportedOperationException("TODO: softDelete");
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    public static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User u = new User(
                rs.getString("user_code"),
                rs.getString("email"),
                rs.getString("display_name"),
                UserRole.valueOf(rs.getString("role"))
        );
        u.setId(rs.getLong("id"));
        u.setIsCurrent(rs.getBoolean("is_current"));
        u.setIsDeleted(rs.getBoolean("is_deleted"));
        u.setEffectiveFrom(rs.getObject("effective_from", LocalDateTime.class));
        u.setEffectiveTo(rs.getObject("effective_to", LocalDateTime.class));
        u.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return u;
    };
}
