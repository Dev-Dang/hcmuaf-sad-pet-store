package hcmuaf.sad.pet_store.uc1;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "uc1"})
@Transactional
class UserTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // [1.1.5] Kiểm tra email chưa tồn tại trong hệ thống
    @Test
    void existsByEmail_nonExistent_shouldReturnFalse() {
        assertThat(User.existsByEmail("nonexistent@test.com")).isFalse();
    }

    // [1.1.5] + [1.1.6] sau insert → existsByEmail = true
    @Test
    void existsByEmail_afterInsert_shouldReturnTrue() {
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "exists@test.com", "Test User", UserRole.CUSTOMER).insert();
        assertThat(User.existsByEmail("exists@test.com")).isTrue();
    }

    // [1.1.5 BR7] tombstone (is_current=true, is_deleted=true) vẫn bị giữ — không cho đăng ký lại
    @Test
    void existsByEmail_tombstone_shouldReturnTrue() {
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        jdbcTemplate.update(
                "INSERT INTO users (user_code, email, display_name, role, effective_from, is_current, is_deleted, created_at) VALUES (?, ?, ?, ?, ?, true, true, ?)",
                userCode, "tombstone@test.com", "Deleted User", "CUSTOMER", LocalDateTime.now(), LocalDateTime.now());
        assertThat(User.existsByEmail("tombstone@test.com")).isTrue();
    }

    // [1.1.6] Tạo tài khoản Customer mới — verify từng field trong DB
    @Test
    void insert_shouldPersistAllFields() {
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "newuser@test.com", "New User", UserRole.CUSTOMER).insert();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM users WHERE user_code = ? AND is_current = true", userCode);

        assertThat(row.get("email")).isEqualTo("newuser@test.com");
        assertThat(row.get("display_name")).isEqualTo("New User");
        assertThat(row.get("role")).isEqualTo("CUSTOMER");
        assertThat((Boolean) row.get("is_current")).isTrue();
        assertThat((Boolean) row.get("is_deleted")).isFalse();
    }
}
