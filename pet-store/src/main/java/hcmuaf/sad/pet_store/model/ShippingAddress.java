package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.model.base.BaseEntity;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ShippingAddress extends BaseEntity {
    private String addressId;
    private String userCode;
    private String recipientName;
    private String phone;
    private String placeId;
    private String fullAddress;
    private String addressDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean isDefault;

    public static List<ShippingAddress> findAllByUserCode(String userCode) {
        try {
            // [8.1.2] Truy xuất danh sách địa chỉ của Customer
            return DBUtils.jdbc().query("""
                    SELECT * FROM shipping_addresses
                    WHERE user_code = ? AND is_current = true AND is_deleted = false
                    ORDER BY is_default DESC, created_at DESC
                    """, ROW_MAPPER, userCode);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static ShippingAddress findByAddressId(String addressId) {
        try {
            // [8.3.1] Truy xuất chi tiết địa chỉ được chọn
            List<ShippingAddress> results = DBUtils.jdbc().query("""
                    SELECT * FROM shipping_addresses
                    WHERE address_id = ? AND is_current = true AND is_deleted = false
                    """, ROW_MAPPER, addressId);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static ShippingAddress findByAddressIdAndUserCode(String addressId, String userCode) {
        try {
            // [8.3.1] Truy xuất chi tiết địa chỉ được chọn theo đúng Customer sở hữu
            List<ShippingAddress> results = DBUtils.jdbc().query("""
                    SELECT * FROM shipping_addresses
                    WHERE address_id = ? AND user_code = ? AND is_current = true AND is_deleted = false
                    """, ROW_MAPPER, addressId, userCode);
            if (results.isEmpty()) {
                throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
            }
            return results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static ShippingAddress findDefaultByUserCode(String userCode) {
        try {
            List<ShippingAddress> results = DBUtils.jdbc().query("""
                    SELECT * FROM shipping_addresses
                    WHERE user_code = ? AND is_default = true AND is_current = true AND is_deleted = false
                    """, ROW_MAPPER, userCode);
            return results.isEmpty() ? null : results.get(0);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void insert() {
        try {
            // [8.2.4] Lưu địa chỉ giao hàng mới
            LocalDateTime now = LocalDateTime.now();
            insertVersion(now, false);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void createForUser(String userCode) {
        try {
            DBUtils.tx().executeWithoutResult(status -> {
                // [8.2.4] Kiểm tra Customer đã có địa chỉ nào chưa
                boolean firstAddress = findAllByUserCode(userCode).isEmpty();

                // [8.2.4] Chuẩn bị dữ liệu địa chỉ mới
                this.addressId = BusinessKeyGenerator.next(EntityType.ADDRESS);
                this.userCode = userCode;
                this.isDefault = firstAddress;

                // [8.2.4] Lưu địa chỉ giao hàng mới
                insertVersion(LocalDateTime.now(), false);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void updateDetails() {
        try {
            // [8.3.5] Cập nhật địa chỉ theo SCD Type 2
            DBUtils.tx().executeWithoutResult(status -> {
                LocalDateTime now = LocalDateTime.now();
                closeCurrentVersion(now);
                insertVersion(now, false);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public void setAsDefault() {
        try {
            // [8.5.3] Cập nhật cờ mặc định để chỉ có đúng một địa chỉ mặc định
            DBUtils.tx().executeWithoutResult(status -> {
                int updatedRows = DBUtils.jdbc().update("""
                        UPDATE shipping_addresses
                        SET is_default = true
                        WHERE address_id = ? AND user_code = ? AND is_current = true AND is_deleted = false
                        """, addressId, userCode);
                if (updatedRows == 0) {
                    throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
                }
                DBUtils.jdbc().update("""
                        UPDATE shipping_addresses
                        SET is_default = false
                        WHERE user_code = ? AND address_id <> ? AND is_default = true
                          AND is_current = true AND is_deleted = false
                        """, userCode, addressId);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    @Override
    public void softDelete() {
        if (isDefault) {
            throw new BusinessException(ErrorCode.ADDRESS_IS_DEFAULT);
        }

        try {
            // [8.4.4] Xóa mềm địa chỉ được chọn bằng SCD tombstone
            DBUtils.tx().executeWithoutResult(status -> {
                LocalDateTime now = LocalDateTime.now();
                closeCurrentVersion(now);
                insertVersion(now, true);
            });
        } catch (DataAccessException | TransactionException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private void closeCurrentVersion(LocalDateTime effectiveTo) {
        int updatedRows = DBUtils.jdbc().update("""
                UPDATE shipping_addresses
                SET is_current = false, effective_to = ?
                WHERE address_id = ? AND user_code = ? AND is_current = true AND is_deleted = false
                """, effectiveTo, addressId, userCode);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    private void insertVersion(LocalDateTime effectiveFrom, boolean deleted) {
        DBUtils.jdbc().update("""
                INSERT INTO shipping_addresses
                (address_id, user_code, recipient_name, phone, place_id, full_address,
                 address_detail, latitude, longitude, is_default,
                 effective_from, is_current, is_deleted, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true, ?, ?)
                """,
                addressId, userCode, recipientName, phone, placeId, fullAddress,
                addressDetail, latitude, longitude, isDefault, effectiveFrom, deleted, effectiveFrom);
    }

    public static final RowMapper<ShippingAddress> ROW_MAPPER = (rs, rowNum) -> {
        ShippingAddress address = new ShippingAddress();
        address.setId(rs.getLong("id"));
        address.setAddressId(rs.getString("address_id"));
        address.setUserCode(rs.getString("user_code"));
        address.setRecipientName(rs.getString("recipient_name"));
        address.setPhone(rs.getString("phone"));
        address.setPlaceId(rs.getString("place_id"));
        address.setFullAddress(rs.getString("full_address"));
        address.setAddressDetail(rs.getString("address_detail"));
        address.setLatitude(rs.getBigDecimal("latitude"));
        address.setLongitude(rs.getBigDecimal("longitude"));
        address.setDefault(rs.getBoolean("is_default"));
        address.setEffectiveFrom(rs.getObject("effective_from", LocalDateTime.class));
        address.setEffectiveTo(rs.getObject("effective_to", LocalDateTime.class));
        address.setIsCurrent(rs.getBoolean("is_current"));
        address.setIsDeleted(rs.getBoolean("is_deleted"));
        address.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return address;
    };
}
