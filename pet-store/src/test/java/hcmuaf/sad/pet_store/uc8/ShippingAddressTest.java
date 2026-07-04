package hcmuaf.sad.pet_store.uc8;

import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.model.ShippingAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void createForUser_firstAddress_shouldCreateDefaultAddress() {
        ShippingAddress address = sampleAddress(null, null, false);

        // [8.2.4] Lưu địa chỉ đầu tiên và tự đặt mặc định
        address.createForUser("KHG-UC8-FIRST");

        List<ShippingAddress> addresses = ShippingAddress.findAllByUserCode("KHG-UC8-FIRST");
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).getAddressId()).isNotBlank();
        assertThat(addresses.get(0).isDefault()).isTrue();
    }

    @Test
    void createForUser_secondAddress_shouldNotCreateDefaultAddress() {
        ShippingAddress first = sampleAddress(null, null, false);
        ShippingAddress second = sampleAddress(null, null, false);
        first.createForUser("KHG-UC8-SECOND");

        // [8.2.4] Customer đã có địa chỉ nên địa chỉ sau không tự là mặc định
        second.createForUser("KHG-UC8-SECOND");

        List<ShippingAddress> addresses = ShippingAddress.findAllByUserCode("KHG-UC8-SECOND");
        long defaultCount = addresses.stream().filter(ShippingAddress::isDefault).count();
        assertThat(addresses).hasSize(2);
        assertThat(defaultCount).isEqualTo(1);
        assertThat(addresses).anyMatch(address -> !address.isDefault());
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
    void softDelete_defaultAddress_shouldBeBlockedByModel() {
        ShippingAddress address = sampleAddress("DCH-0000006", "KHG-0000006", true);
        address.insert();

        // [8.4.3] Không cho xóa địa chỉ mặc định
        assertThatThrownBy(address::softDelete)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ADDRESS_IS_DEFAULT);
    }

    @Test
    void setAsDefault_shouldSwitchDefaultWithoutCreatingNewVersion() {
        ShippingAddress first = sampleAddress("DCH-0000004", "KHG-0000004", true);
        ShippingAddress second = sampleAddress("DCH-0000005", "KHG-0000004", false);
        first.insert();
        second.insert();

        // [8.5.3] Cập nhật cờ mặc định để chỉ có đúng một địa chỉ mặc định
        second.setAsDefault();

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
