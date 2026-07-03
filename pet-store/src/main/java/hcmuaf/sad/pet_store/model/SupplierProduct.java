package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * Chiếu (projection) tối thiểu của bảng products, chỉ dùng để hiển thị
 * "sản phẩm của nhà cung cấp" trong trang chi tiết NCC — không thay thế
 * cho entity Product (JPA) hiện có.
 */
@Getter
@Setter
public class SupplierProduct {
    private Long id;
    private String name;
    private String status;

    public SupplierProduct(Long id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public static List<SupplierProduct> findBySupplierId(Long supplierId) {
        try {
            return DBUtils.jdbc().query("""
                    SELECT id, name, status FROM products
                    WHERE supplier_id = ? AND deleted_at IS NULL
                    ORDER BY name ASC
                    """, ROW_MAPPER, supplierId);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private static final RowMapper<SupplierProduct> ROW_MAPPER = (rs, rowNum) -> new SupplierProduct(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("status")
    );
}