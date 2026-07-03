package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.model.base.BaseEntity;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionException;

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
        try {
            Integer count = DBUtils.jdbc().queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = ? AND is_current = true",
                    Integer.class, email);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void insert() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    INSERT INTO users (user_code, email, display_name, role, effective_from, is_current, is_deleted, created_at)
                    VALUES (?, ?, ?, ?, ?, true, false, ?)
                    """,
                    userCode, email, displayName, role.name(), now, now);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    // ─── TODO: UC-2/3/4 ───────────────────────────────────────────────────────

    public static User findActiveByEmail(String email) {
        try {
            List<User> results = DBUtils.jdbc().query(
                    "SELECT * FROM users WHERE email = ? AND is_current = true AND is_deleted = false",
                    ROW_MAPPER, email);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static User findActiveByUserCode(String userCode) {
        try {
            List<User> results = DBUtils.jdbc().query(
                    "SELECT * FROM users WHERE user_code = ? AND is_current = true AND is_deleted = false",
                    ROW_MAPPER, userCode);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static List<User> findActiveCustomers(int page, int size) {
        try {
            int offset = offset(page, size);
            return DBUtils.jdbc().query("""
                    SELECT * FROM users
                    WHERE role = 'CUSTOMER' AND is_current = true AND is_deleted = false
                    ORDER BY created_at DESC
                    LIMIT ? OFFSET ?
                    """, ROW_MAPPER, size, offset);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static int countActiveCustomers() {
        try {
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM users
                    WHERE role = 'CUSTOMER' AND is_current = true AND is_deleted = false
                    """, Integer.class);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static List<User> searchActiveCustomers(String keyword, int page, int size) {
        try {
            int offset = offset(page, size);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return DBUtils.jdbc().query("""
                    SELECT * FROM users
                    WHERE role = 'CUSTOMER' AND is_current = true AND is_deleted = false
                      AND (LOWER(display_name) LIKE ? OR LOWER(email) LIKE ?)
                    ORDER BY created_at DESC
                    LIMIT ? OFFSET ?
                    """, ROW_MAPPER, pattern, pattern, size, offset);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static int countActiveCustomersByKeyword(String keyword) {
        try {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM users
                    WHERE role = 'CUSTOMER' AND is_current = true AND is_deleted = false
                      AND (LOWER(display_name) LIKE ? OR LOWER(email) LIKE ?)
                    """, Integer.class, pattern, pattern);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private static int offset(int page, int size) {
        int safePage = Math.max(page, 1);
        return (safePage - 1) * size;
    }

    public static User findActiveById(Long id) {
        try {
            List<User> results = DBUtils.jdbc().query(
                    "SELECT * FROM users WHERE id = ? AND is_current = true AND is_deleted = false",
                    ROW_MAPPER, id);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static User findById(Long id) {
        try {
            List<User> results = DBUtils.jdbc().query(
                    "SELECT * FROM users WHERE id = ?",
                    ROW_MAPPER, id);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void updateDisplayName(String name) {
        // TODO: SCD Type 2 update
        throw new UnsupportedOperationException("TODO: SCD update");
    }

    @Override
    public void softDelete() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.tx().executeWithoutResult(status -> {
                DBUtils.jdbc().update(
                        "UPDATE users SET is_current = false, effective_to = ? WHERE user_code = ? AND is_current = true",
                        now, userCode);
                DBUtils.jdbc().update("""
                        INSERT INTO users (user_code, email, display_name, role, effective_from, is_current, is_deleted, created_at)
                        VALUES (?, ?, ?, ?, ?, true, true, ?)
                        """,
                        userCode, email, displayName, role.name(), now, now);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
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
