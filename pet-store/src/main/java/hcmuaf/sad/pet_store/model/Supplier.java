package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Nhà cung cấp — fact table (không cần lưu lịch sử thay đổi), UPDATE trực tiếp.
 * Soft-delete bằng cột deleted_at, cùng convention với Category/Product.
 */
@Getter
@Setter
public class Supplier {
    private Long id;
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Supplier() {
    }

    // ─── Thêm mới NCC ───────────────────────────────────────────────────────

    public void insert() {
        try {
            LocalDateTime now = LocalDateTime.now();
            DBUtils.jdbc().update("""
                    INSERT INTO suppliers (supplier_code, name, contact_person, phone, email, address, is_active, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    supplierCode, name, contactPerson, phone, email, address, isActive, now, now);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    // ─── Truy vấn ────────────────────────────────────────────────────────────

    public static boolean existsByName(String name) {
        try {
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM suppliers
                    WHERE LOWER(name) = LOWER(?) AND deleted_at IS NULL
                    """, Integer.class, name);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static boolean existsByNameExcludingId(String name, Long excludeId) {
        try {
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM suppliers
                    WHERE LOWER(name) = LOWER(?) AND deleted_at IS NULL AND id <> ?
                    """, Integer.class, name, excludeId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static Supplier findById(Long id) {
        try {
            List<Supplier> results = DBUtils.jdbc().query(
                    "SELECT * FROM suppliers WHERE id = ? AND deleted_at IS NULL",
                    ROW_MAPPER, id);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static List<Supplier> findAll(int page, int size) {
        try {
            int offset = offset(page, size);
            return DBUtils.jdbc().query("""
                    SELECT * FROM suppliers
                    WHERE deleted_at IS NULL
                    ORDER BY created_at DESC
                    LIMIT ? OFFSET ?
                    """, ROW_MAPPER, size, offset);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static int countAll() {
        try {
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM suppliers WHERE deleted_at IS NULL
                    """, Integer.class);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static List<Supplier> search(String keyword, int page, int size) {
        try {
            int offset = offset(page, size);
            String pattern = "%" + keyword.toLowerCase() + "%";
            return DBUtils.jdbc().query("""
                    SELECT * FROM suppliers
                    WHERE deleted_at IS NULL
                      AND (LOWER(name) LIKE ? OR LOWER(supplier_code) LIKE ? OR LOWER(contact_person) LIKE ? OR phone LIKE ?)
                    ORDER BY created_at DESC
                    LIMIT ? OFFSET ?
                    """, ROW_MAPPER, pattern, pattern, pattern, pattern, size, offset);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static int countByKeyword(String keyword) {
        try {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM suppliers
                    WHERE deleted_at IS NULL
                      AND (LOWER(name) LIKE ? OR LOWER(supplier_code) LIKE ? OR LOWER(contact_person) LIKE ? OR phone LIKE ?)
                    """, Integer.class, pattern, pattern, pattern, pattern);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    // ─── Cập nhật / Xóa (fact table — UPDATE trực tiếp) ────────────────────────

    public void update() {
        try {
            DBUtils.jdbc().update("""
                    UPDATE suppliers
                    SET name = ?, contact_person = ?, phone = ?, email = ?, address = ?, is_active = ?, updated_at = ?
                    WHERE id = ? AND deleted_at IS NULL
                    """,
                    name, contactPerson, phone, email, address, isActive, LocalDateTime.now(), id);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void softDelete() {
        try {
            DBUtils.jdbc().update(
                    "UPDATE suppliers SET deleted_at = ?, is_active = false, updated_at = ? WHERE id = ?",
                    LocalDateTime.now(), LocalDateTime.now(), id);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    // ─── Sản phẩm của nhà cung cấp ──────────────────────────────────────────────

    public static int countActiveProducts(Long supplierId) {
        try {
            Integer count = DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) FROM products
                    WHERE supplier_id = ? AND deleted_at IS NULL
                    """, Integer.class, supplierId);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private static int offset(int page, int size) {
        int safePage = Math.max(page, 1);
        return (safePage - 1) * size;
    }

    // ─── RowMapper ────────────────────────────────────────────────────────────

    public static final RowMapper<Supplier> ROW_MAPPER = (rs, rowNum) -> {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setSupplierCode(rs.getString("supplier_code"));
        s.setName(rs.getString("name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setActive(rs.getBoolean("is_active"));
        s.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        s.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        s.setDeletedAt(rs.getObject("deleted_at", LocalDateTime.class));
        return s;
    };
}