package hcmuaf.sad.pet_store.uc8;

import hcmuaf.sad.pet_store.model.ShippingAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShippingAddressTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void insert_shouldCreateCurrentActiveAddress() {
        ShippingAddress address = sampleAddress("DCH-0000001", "KHG-0000001", true);

        // [8.2.4] Lưu địa chỉ giao hàng mới
        address.insert();

        ShippingAddress found = ShippingAddress.findByAddressId("DCH-0000001");
        assertThat(found).isNotNull();
        assertThat(found.getUserCode()).isEqualTo("KHG-0000001");
        assertThat(found.isDefault()).isTrue();
        assertThat(found.isCurrent()).isTrue();
        assertThat(found.isDeleted()).isFalse();
    }

    @Test
    void updateDetails_shouldCloseOldVersionAndInsertCurrentVersion() {
        ShippingAddress address = sampleAddress("DCH-0000002", "KHG-0000002", true);
        address.insert();

        address.setRecipientName("Tran Van B");
        address.setPhone("0911111111");
        address.setAddressDetail("Tang 2");

        // [8.3.5] Cập nhật địa chỉ theo SCD Type 2
        address.updateDetails();

        Integer totalRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shipping_addresses WHERE address_id = ?",
                Integer.class, "DCH-0000002");
        Integer currentRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shipping_addresses WHERE address_id = ? AND is_current = true",
                Integer.class, "DCH-0000002");
        ShippingAddress found = ShippingAddress.findByAddressId("DCH-0000002");

        assertThat(totalRows).isEqualTo(2);
        assertThat(currentRows).isEqualTo(1);
        assertThat(found.getRecipientName()).isEqualTo("Tran Van B");
        assertThat(found.isDefault()).isTrue();
    }

    @Test
    void softDelete_shouldCreateCurrentTombstoneAndHideFromBusinessQueries() {
        ShippingAddress address = sampleAddress("DCH-0000003", "KHG-0000003", false);
        address.insert();

        // [8.4.4] Xóa mềm địa chỉ được chọn bằng SCD tombstone
        address.softDelete();

        ShippingAddress found = ShippingAddress.findByAddressId("DCH-0000003");
        Integer tombstones = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM shipping_addresses
                WHERE address_id = ? AND is_current = true AND is_deleted = true
                """, Integer.class, "DCH-0000003");

        assertThat(found).isNull();
        assertThat(tombstones).isEqualTo(1);
    }

    @Test
    void updateDefaultAddress_shouldSwitchDefaultWithoutCreatingNewVersion() {
        ShippingAddress first = sampleAddress("DCH-0000004", "KHG-0000004", true);
        ShippingAddress second = sampleAddress("DCH-0000005", "KHG-0000004", false);
        first.insert();
        second.insert();

        // [8.5.3] Cập nhật cờ mặc định để chỉ có đúng một địa chỉ mặc định
        ShippingAddress.updateDefaultAddress("KHG-0000004", "DCH-0000005");

        Integer defaultRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM shipping_addresses
                WHERE user_code = ? AND is_current = true AND is_deleted = false AND is_default = true
                """, Integer.class, "KHG-0000004");
        Integer secondVersions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shipping_addresses WHERE address_id = ?",
                Integer.class, "DCH-0000005");

        assertThat(defaultRows).isEqualTo(1);
        assertThat(ShippingAddress.findDefaultByUserCode("KHG-0000004").getAddressId()).isEqualTo("DCH-0000005");
        assertThat(secondVersions).isEqualTo(1);
    }

    private ShippingAddress sampleAddress(String addressId, String userCode, boolean isDefault) {
        ShippingAddress address = new ShippingAddress();
        address.setAddressId(addressId);
        address.setUserCode(userCode);
        address.setRecipientName("Nguyen Van A");
        address.setPhone("0900000000");
        address.setPlaceId("place-id");
        address.setFullAddress("Linh Trung, Thu Duc, TP HCM");
        address.setAddressDetail("So 1");
        address.setLatitude(new BigDecimal("10.87000000"));
        address.setLongitude(new BigDecimal("106.80000000"));
        address.setDefault(isDefault);
        return address;
    }
}
